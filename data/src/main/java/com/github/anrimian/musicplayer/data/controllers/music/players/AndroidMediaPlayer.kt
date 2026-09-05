package com.github.anrimian.musicplayer.data.controllers.music.players

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder
import com.github.anrimian.musicplayer.data.models.composition.source.UriContentSource
import com.github.anrimian.musicplayer.data.utils.hasPersistedReadPermission
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.NoReadPermissionException
import com.github.anrimian.musicplayer.domain.models.composition.content.RelaunchSourceException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnknownPlayerException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class AndroidMediaPlayer(
    private val context: Context,
    private val ioScheduler: Scheduler,
    private val equalizerController: EqualizerController,
    private val sourceBuilder: MediaPlayerDataSourceBuilder,
    private val analytics: Analytics
) : AppMediaPlayer {

    private val playerEventsSubject = PublishSubject.create<MediaPlayerEvent>()

    private val mediaPlayer = MediaPlayer().apply {
        setOnCompletionListener { onPlaybackFinished() }
        setOnErrorListener { _, what, extra ->
            val ex = createExceptionFromPlayerError(what, extra)
            playerEventsSubject.onNext(MediaPlayerEvent.Error(ex))
            false
        }
    }

    private var currentSource: CompositionContentSource? = null

    private var isSourcePrepared = false
    private var isPlaying = false

    private var previousException: Exception? = null
    private var postponedPosition: Long? = null

    private var volume = 1f
    private var leftVolume = 1f
    private var rightVolume = 1f

    private var trackEndMonitorDisposable: Disposable? = null
    private val isFinished = AtomicBoolean(false)

    override fun prepareToPlay(
        source: CompositionContentSource,
        previousException: Exception?,
    ): Completable {
        stopTrackEndMonitor()
        isFinished.set(false)
        currentSource = source
        postponedPosition = null
        this.previousException = previousException
        return prepareMediaSource(source)
            .doOnSubscribe { isSourcePrepared = false }
            .doOnComplete {
                isSourcePrepared = true
                postponedPosition?.let(this::seekTo)
            }
            .onErrorResumeNext { t -> Completable.error(mapPrepareException(t, source)) }
            .subscribeOn(ioScheduler)
    }

    override fun stop() {
        if (!isPlaying) {
            return
        }
        stopTrackEndMonitor()
        if (isSourcePrepared) {
            seekTo(0)
        }
        if (isSourcePrepared) {
            pausePlayer()
        }
        isPlaying = false
    }

    override fun resume() {
        if (isPlaying) {
            return
        }
        if (isSourcePrepared) {
            start()
        }
    }

    override fun pause() {
        if (!isPlaying) {
            return
        }
        stopTrackEndMonitor()
        pausePlayer()
        isPlaying = false
    }

    override fun seekTo(position: Long) {
        try {
            if (isSourcePrepared) {
                synchronized(mediaPlayer) {
                    mediaPlayer.seekTo(position.toInt())
                    // If logical state is playing but physical playback stopped (e.g. track ended),
                    // we must restart it to handle track repeats correctly
                    if (isPlaying && !mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                        isFinished.set(false)
                        startTrackEndMonitor()
                    }
                }
            } else if (currentSource != null) {
                postponedPosition = position
            }
        } catch (ex: Exception) {
            val exception = if (ex is SecurityException) {
                ex
            } else {
                RelaunchSourceException(ex)
            }
            playerEventsSubject.onNext(MediaPlayerEvent.Error(exception))
        }
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
        applyVolume()
    }

    override fun getTrackPositionObservable(): Observable<Long> {
        return Observable.interval(0, 50, TimeUnit.MILLISECONDS)
            .observeOn(ioScheduler)
            .flatMapSingle { getTrackPosition() }
    }

    override fun getTrackPosition(): Single<Long> {
        return Single.fromCallable {
            if (!isSourcePrepared) {
                return@fromCallable 0L
            }
            try {
                synchronized(mediaPlayer) {
                    return@fromCallable mediaPlayer.currentPosition.toLong()
                }
            } catch (_: IllegalStateException) {
                return@fromCallable 0L
            }
        }
    }

    override fun getDuration(): Single<Long> {
        return Single.fromCallable {
            if (!isSourcePrepared) {
                return@fromCallable 0L
            }
            try {
                synchronized(mediaPlayer) {
                    return@fromCallable mediaPlayer.duration.toLong()
                }
            } catch (_: IllegalStateException) {
                return@fromCallable 0L
            }
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                synchronized(mediaPlayer) {
                    val isPlaying = mediaPlayer.isPlaying
                    mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed)
                    if (!isPlaying) {
                        mediaPlayer.pause()
                    }
                }
            } catch (_: IllegalStateException) {
            } catch (_: IllegalArgumentException) {
            } //IllegalArgumentException - reset ui
        }
    }

    override fun setSkipSilenceEnabled(enabled: Boolean) {
        //not supported
    }

    override fun release() {
        synchronized(mediaPlayer) {
            stopTrackEndMonitor()
            isSourcePrepared = false
            currentSource = null
            equalizerController.detachEqualizer()
            mediaPlayer.release()
        }
    }

    override fun getPlayerEventsObservable(): Observable<MediaPlayerEvent> {
        return playerEventsSubject
    }

    override fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return Observable.fromCallable { Build.VERSION.SDK_INT >= Build.VERSION_CODES.M }
    }

    override fun setSoundBalance(soundBalance: SoundBalance) {
        leftVolume = soundBalance.left
        rightVolume = soundBalance.right
        applyVolume()
    }

    private fun applyVolume() {
        val leftOutput = volume * leftVolume
        val rightOutput = volume * rightVolume
        try {
            synchronized(mediaPlayer) {
                mediaPlayer.setVolume(leftOutput, rightOutput)
            }
        } catch (_: IllegalStateException) {}
    }

    private fun createExceptionFromPlayerError(what: Int, playerError: Int): Exception {
        return when (playerError) {
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED,
            MediaPlayer.MEDIA_ERROR_MALFORMED,
            -> UnsupportedSourceException()
            else -> {
                val message = "unknown media player error, what: $what, extra: $playerError"
                UnknownPlayerException(message)
            }
        }
    }

    private fun mapPrepareException(throwable: Throwable, source: CompositionContentSource): Throwable {
        if (throwable is IOException) {
            if (source is UriContentSource && !source.uri.hasPersistedReadPermission(context)) {
                return NoReadPermissionException(throwable)
            }
        }
        
        if (previousException is UnsupportedSourceException) {
            previousException = null
            return UnsupportedSourceException()
        }

        return throwable
    }

    private fun prepareMediaSource(source: CompositionContentSource): Completable {
        return Completable.create { emitter ->
            synchronized(mediaPlayer) {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    sourceBuilder.setMediaSource(mediaPlayer, source)

                    mediaPlayer.setOnPreparedListener {
                        if (!emitter.isDisposed) {
                            emitter.onComplete()
                        }
                    }
                    mediaPlayer.setOnErrorListener { _, what, extra ->
                        isSourcePrepared = false
                        val ex = createExceptionFromPlayerError(what, extra)
                        if (!emitter.isDisposed) {
                            emitter.tryOnError(ex)
                        }
                        true // return true to ignore error
                    }
                    mediaPlayer.prepareAsync()
                } catch (t: Throwable) {
                    if (!emitter.isDisposed) {
                        emitter.tryOnError(t)
                    }
                }
            }
        }
    }

    private fun pausePlayer() {
        try {
            synchronized(mediaPlayer) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    equalizerController.detachEqualizer()
                }
            }
        } catch (_: Exception) {}
    }

    private fun start() {
        synchronized(mediaPlayer) {
            try {
                mediaPlayer.start()
                startTrackEndMonitor()
                equalizerController.attachEqualizer(mediaPlayer.audioSessionId)
                isPlaying = true
            } catch (_: IllegalStateException) {}
        }
    }

    private fun onPlaybackFinished() {
        if (isFinished.compareAndSet(false, true)) {
            stopTrackEndMonitor()
            playerEventsSubject.onNext(MediaPlayerEvent.Finished)
        }
    }

    private fun startTrackEndMonitor() {
        stopTrackEndMonitor()
        trackEndMonitorDisposable = getDuration()
            .flatMapObservable { duration ->
                if (duration <= 0) {
                    return@flatMapObservable Observable.empty()
                }
                Observable.interval(0, 500, TimeUnit.MILLISECONDS, ioScheduler)
                    .flatMapSingle { getTrackPosition() }
                    .doOnNext { position ->
                        if (position >= duration - PLAYBACK_END_TOLERANCE_WINDOW_MS) {
                            onPlaybackFinished()
                        }
                    }
            }
            .doOnError { t -> analytics.processNonFatalError(t, "Track end monitor") }
            .onErrorComplete()
            .subscribe()
    }

    private fun stopTrackEndMonitor() {
        trackEndMonitorDisposable?.dispose()
        trackEndMonitorDisposable = null
    }


    private companion object {
        /**
         * A tolerance window in milliseconds to account for inaccuracies in MediaPlayer's
         * position reporting near the end of a track.
         */
        private const val PLAYBACK_END_TOLERANCE_WINDOW_MS = 200L
    }

}
