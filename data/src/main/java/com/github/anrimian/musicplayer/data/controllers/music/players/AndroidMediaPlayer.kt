package com.github.anrimian.musicplayer.data.controllers.music.players

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.UnknownPlayerException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AndroidMediaPlayer(
    private val uiScheduler: Scheduler,
    private val equalizerController: EqualizerController,
    private val sourceBuilder: MediaPlayerDataSourceBuilder,
) : AppMediaPlayer {

    private val playerEventsSubject = PublishSubject.create<MediaPlayerEvent>()

    private val mediaPlayer = MediaPlayer().apply {
        //problem with error case(file not found), multiple error events
        setOnCompletionListener { playerEventsSubject.onNext(MediaPlayerEvent.Finished) }
        setOnErrorListener { _, what, extra ->
            val ex = createExceptionFromPlayerError(what, extra)
            playerEventsSubject.onNext(MediaPlayerEvent.Error(ex))
            false
        }

//        try {
//            equalizerController.attachEqualizer(mediaPlayer.getAudioSessionId());
//        } catch (IllegalStateException ignored) {}
    }

    private var isSourcePrepared = false// is it necessary now?
    private var isPlaying = false

    private var previousException: Exception? = null

    private var volume = 1f
    private var leftVolume = 1f
    private var rightVolume = 1f

    override fun prepareToPlay(
        source: CompositionContentSource,
        previousException: Exception?,
    ): Completable {
        this.previousException = previousException
        return prepareMediaSource(source)
            .doOnSubscribe { isSourcePrepared = false }
            .doOnComplete { isSourcePrepared = true }
            .onErrorResumeNext { t -> Completable.error(mapPrepareException(t)) }
            .subscribeOn(uiScheduler)
    }

    override fun stop() {
        if (!isPlaying) {
            return
        }
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
        pausePlayer()
        isPlaying = false
    }

    override fun seekTo(position: Long) {
        try {
            if (isSourcePrepared) {
                mediaPlayer.seekTo(position.toInt())
            }
        } catch (ignored: IllegalStateException) {}
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
        applyVolume()
    }

    override fun getTrackPositionObservable(): Observable<Long> {
        return Observable.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(uiScheduler)
            .flatMapSingle { getTrackPosition() }
    }

    override fun getTrackPosition(): Single<Long> {
        return Single.fromCallable {
            if (!isSourcePrepared) {
                return@fromCallable 0L
            }
            try {
                return@fromCallable mediaPlayer.currentPosition.toLong()
            } catch (e: IllegalStateException) {
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
                return@fromCallable mediaPlayer.duration.toLong()
            } catch (e: IllegalStateException) {
                return@fromCallable 0L
            }
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed)
            } catch (ignored: IllegalStateException) {
            } //IllegalArgumentException - handle unsupported case
        }
    }

    override fun release() {
        equalizerController.detachEqualizer()
        mediaPlayer.release()
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
            mediaPlayer.setVolume(leftOutput, rightOutput)
        } catch (ignored: IllegalStateException) {}
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

    private fun mapPrepareException(throwable: Throwable): Throwable {
        if (throwable is IOException && previousException is UnsupportedSourceException) {
            previousException = null
            return UnsupportedSourceException()
        }
        return throwable
    }

    private fun prepareMediaSource(source: CompositionContentSource): Completable {
        return Completable.fromAction {
            mediaPlayer.reset()
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            sourceBuilder.setMediaSource(mediaPlayer, source)
            mediaPlayer.prepare()
        }
    }

    private fun pausePlayer() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                equalizerController.detachEqualizer()
            }
        } catch (ignored: Exception) {}
    }

    private fun start() {
        start(mediaPlayer)
        isPlaying = true
    }

    private fun start(mediaPlayer: MediaPlayer) {
        try {
            mediaPlayer.start()
            equalizerController.attachEqualizer(mediaPlayer.audioSessionId)
        } catch (ignored: IllegalStateException) {
        }
    }

}