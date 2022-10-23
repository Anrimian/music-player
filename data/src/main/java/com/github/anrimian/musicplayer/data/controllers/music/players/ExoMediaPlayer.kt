package com.github.anrimian.musicplayer.data.controllers.music.players

import android.content.Context
import android.net.Uri
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.controllers.music.players.exoplayer.StereoVolumeProcessor
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder
import com.github.anrimian.musicplayer.data.utils.exo_player.PlayerEventListener
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.audio.AudioProcessor
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException
import com.google.android.exoplayer2.upstream.DefaultDataSource
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class ExoMediaPlayer(
    private val context: Context,
    private val uiScheduler: Scheduler,
    private val equalizerController: EqualizerController,
    private val exoPlayerMediaItemBuilder: ExoPlayerMediaItemBuilder
) : AppMediaPlayer {

    private val playerEventsSubject = PublishSubject.create<MediaPlayerEvent>()
    private val stereoVolumeProcessor = StereoVolumeProcessor().apply {
        setChannelMap(intArrayOf(0, 1))
    }

    private val player by lazy {
        val factory = createSimpleRenderersFactory(context, stereoVolumeProcessor)
        val player = ExoPlayer.Builder(context, factory).build()
        val playerEventListener = PlayerEventListener(
            { playerEventsSubject.onNext(MediaPlayerEvent.Finished) },
            { t -> playerEventsSubject.onNext(MediaPlayerEvent.Error(mapPlayerException(t)))})
        player.addListener(playerEventListener)
//      equalizerController.attachEqualizer(player.getAudioSessionId());
//          player.addAnalyticsListener(new AnalyticsListener() {
//              @Override
//              public void onAudioSessionIdChanged(@NonNull EventTime eventTime, int audioSessionId) {
//                  equalizerController.attachEqualizer(audioSessionId);
//              }
//      });
        return@lazy player
    }

    override fun getPlayerEventsObservable(): Observable<MediaPlayerEvent> {
        return playerEventsSubject
    }

    override fun prepareToPlay(
        source: CompositionContentSource,
        previousException: Exception?
    ): Completable {
        return Single.just(exoPlayerMediaItemBuilder.createUri(source))
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
            } catch (ignored: IndexOutOfBoundsException) { //crash inside exoplayer
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
        return Observable.interval(0, 1, TimeUnit.SECONDS)
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
        if (throwable is PlaybackException && throwable.cause is UnrecognizedInputFormatException) {
            return UnsupportedSourceException()
        }
        return throwable
    }

    private fun createMediaSource(uri: Uri): Single<MediaSource> {
        return Single.fromCallable<MediaSource> {
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaItem = MediaItem.fromUri(uri)
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }.timeout(6, TimeUnit.SECONDS) //read from uri can be freeze for some reason, check
    }

    private fun usePlayer(function: (ExoPlayer) -> Unit) {
        Completable.fromAction { function(player) }
            .subscribeOn(uiScheduler)
            .subscribe()
    }

    private fun createSimpleRenderersFactory(
        context: Context,
        vararg audioProcessors: AudioProcessor
    ): RenderersFactory {
        return object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context1: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean,
                enableOffload: Boolean
            ): AudioSink {
                return DefaultAudioSink.Builder()
                    .setAudioCapabilities(AudioCapabilities.getCapabilities(context1))
                    .setAudioProcessors(audioProcessors)
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .setOffloadMode(if (enableOffload) {
                        DefaultAudioSink.OFFLOAD_MODE_ENABLED_GAPLESS_REQUIRED
                    } else {
                        DefaultAudioSink.OFFLOAD_MODE_DISABLED
                    }).build()
            }
        }
    }

}