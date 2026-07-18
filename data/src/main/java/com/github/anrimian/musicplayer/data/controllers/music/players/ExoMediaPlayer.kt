package com.github.anrimian.musicplayer.data.controllers.music.players

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import androidx.media3.exoplayer.upstream.Loader
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.AdtsExtractor
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.controllers.music.players.exceptions.PlayerOutOfMemoryException
import com.github.anrimian.musicplayer.data.controllers.music.players.exoplayer.StereoVolumeProcessor
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder
import com.github.anrimian.musicplayer.data.utils.exo_player.PlayerEventListener
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.NoReadPermissionException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

@SuppressLint("UnsafeOptInUsageError")
class ExoMediaPlayer(
    private val context: Context,
    private val uiScheduler: Scheduler,
    private val equalizerController: EqualizerController,
    private val exoPlayerMediaItemBuilder: ExoPlayerMediaItemBuilder,
    private val hasAlternativePlayer: Boolean,
) : AppMediaPlayer {

    private val playerEventsSubject = PublishSubject.create<MediaPlayerEvent>()
    private val stereoVolumeProcessor = StereoVolumeProcessor().apply {
        setChannelMap(intArrayOf(0, 1))
    }
    private val silenceSkippingAudioProcessor = SilenceSkippingAudioProcessor(
        /* minimumSilenceDurationUs = */ 2000_000L,
        /* silenceRetentionRatio = */ 0f,
        /* maxSilenceToKeepDurationUs = */ 2000_000L,
        /* minVolumeToKeepPercentageWhenMuting = */ 0,
        /* silenceThresholdLevel = */ 128.toShort()
    )

    private val player by lazy {
        val factory = createSimpleRenderersFactory(
            context,
            stereoVolumeProcessor,
            silenceSkippingAudioProcessor
        )
        val player = ExoPlayer.Builder(context, factory).build()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player.setAudioAttributes(audioAttributes, false)
        val playerEventListener = PlayerEventListener(
            { playerEventsSubject.onNext(MediaPlayerEvent.Finished) },
            { t -> playerEventsSubject.onNext(MediaPlayerEvent.Error(mapPlayerException(t)))})
        player.addListener(playerEventListener)
        return@lazy player
    }

    override fun getPlayerEventsObservable(): Observable<MediaPlayerEvent> {
        return playerEventsSubject
    }

    override fun prepareToPlay(
        source: CompositionContentSource,
        previousException: Exception?,
    ): Completable {
        return Single.just(exoPlayerMediaItemBuilder.createUri(source))
            .doOnSuccess { uri ->
                if (hasAlternativePlayer && isRawAacFile(uri)) {
                    throw UnsupportedSourceException()
                }
            }
            .flatMap(this::createMediaSource)
            .observeOn(uiScheduler)
            .doOnSuccess { mediaSource ->
                player.setMediaSource(mediaSource)
                player.prepare()
            }
            .ignoreElement()
            .onErrorResumeNext { t -> Completable.error(mapPlayerException(t)) }
    }

    override fun stop() {
        Completable.fromRunnable {
            seekTo(0)
            pausePlayer()
        }.subscribeOn(uiScheduler).subscribe()
    }

    override fun resume() {
        startPlayWhenReady()
    }

    override fun pause() {
        Completable.fromRunnable {
            pausePlayer()
        }.subscribeOn(uiScheduler).subscribe()
    }

    override fun seekTo(position: Long) {
        Completable.fromRunnable {
            try {
                player.seekTo(position)
            } catch (_: IndexOutOfBoundsException) { //crash inside exoplayer
                return@fromRunnable
            }
        }.subscribeOn(uiScheduler).subscribe()
    }

    override fun setVolume(volume: Float) {
        Completable.fromRunnable { player.volume = volume }
            .subscribeOn(uiScheduler)
            .subscribe()
    }

    override fun getTrackPositionObservable(): Observable<Long> {
        return Observable.interval(0, 50, TimeUnit.MILLISECONDS)
            .observeOn(uiScheduler)
            .map { player.currentPosition }
    }

    override fun getTrackPosition(): Single<Long> {
        return Single.fromCallable { player.currentPosition }
            .subscribeOn(uiScheduler)
    }

    override fun getDuration(): Single<Long> {
        return Single.fromCallable { player.duration }
            .subscribeOn(uiScheduler)
    }

    override fun setPlaybackSpeed(speed: Float) {
        usePlayer { player -> player.playbackParameters = PlaybackParameters(speed) }
    }

    override fun setSkipSilenceEnabled(enabled: Boolean) {
        usePlayer { player -> player.skipSilenceEnabled = enabled }
    }

    override fun release() {
        usePlayer { player ->
//            equalizerController.detachEqualizer();
            pausePlayer()
            player.release()
        }
    }

    override fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return Observable.fromCallable { true }
    }

    override fun setSoundBalance(soundBalance: SoundBalance) {
        stereoVolumeProcessor.setVolume(soundBalance.left, soundBalance.right)
    }

    private fun startPlayWhenReady() {
        Completable.fromRunnable {
            player.playWhenReady = true
            equalizerController.attachEqualizer(player.audioSessionId)
        }.subscribeOn(uiScheduler).subscribe()
    }

    private fun pausePlayer() {
        player.playWhenReady = false
        equalizerController.detachEqualizer()
    }

    private fun mapPlayerException(throwable: Throwable): Throwable {
        //logic is duplicated in PlayerErrorParserImpl
        //likely all exo player error parsing logic should be here
        if (throwable is PlaybackException) {
            when (val cause = throwable.cause) {
                is UnrecognizedInputFormatException -> {
                    return UnsupportedSourceException()
                }
                is Loader.UnexpectedLoaderException -> {
                    when (val causeOfCause = cause.cause) {
                        is OutOfMemoryError -> return PlayerOutOfMemoryException()
                        is SecurityException -> return NoReadPermissionException(causeOfCause)
                    }
                }
            }
        }
        return throwable
    }

    private fun createMediaSource(uri: Uri): Single<MediaSource> {
        return Single.fromCallable<MediaSource> {
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val extractorsFactory = DefaultExtractorsFactory()
                .setAdtsExtractorFlags(AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)
            val mediaItem = MediaItem.fromUri(uri)
            ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                .createMediaSource(mediaItem)
        }.timeout(6, TimeUnit.SECONDS) //read from uri can be freeze for some reason, check
    }

    private fun isRawAacFile(uri: Uri): Boolean {
        return uri.lastPathSegment?.endsWith(".aac", ignoreCase = true) == true
    }

    private fun usePlayer(function: (ExoPlayer) -> Unit) {
        Completable.fromAction { function(player) }
            .subscribeOn(uiScheduler)
            .subscribe()
    }

    private fun createSimpleRenderersFactory(
        context: Context,
        stereoProcessor: AudioProcessor,
        silenceProcessor: SilenceSkippingAudioProcessor
    ): RenderersFactory {
        return object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean,
            ): AudioSink {

                val audioProcessorChain = DefaultAudioSink.DefaultAudioProcessorChain(
                    arrayOf(stereoProcessor),
                    silenceProcessor,
                    SonicAudioProcessor()
                )

                return DefaultAudioSink.Builder(context)
                    .setAudioProcessorChain(audioProcessorChain)
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .build()
            }
        }
    }

}