package com.github.anrimian.musicplayer.data.controllers.music

import android.content.Context
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.controllers.music.players.AndroidMediaPlayer
import com.github.anrimian.musicplayer.data.controllers.music.players.AppMediaPlayer
import com.github.anrimian.musicplayer.data.controllers.music.players.CompositeMediaPlayer
import com.github.anrimian.musicplayer.data.controllers.music.players.ExoMediaPlayer
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

class MusicPlayerControllerImpl(
    settingsRepository: SettingsRepository,
    context: Context,
    uiScheduler: Scheduler,
    equalizerController: EqualizerController,
    exoPlayerMediaItemBuilder: ExoPlayerMediaItemBuilder,
    sourceBuilder: MediaPlayerDataSourceBuilder
): MusicPlayerController {

    private val currentSpeedSubject = BehaviorSubject.createDefault(1f)

    private val mediaPlayer: AppMediaPlayer

    init {
        val mediaPlayers = settingsRepository.enabledMediaPlayers
        val mediaPlayerImpls = ArrayList<() -> AppMediaPlayer>(mediaPlayers.size)
        for (playerId in mediaPlayers) {
            when (playerId) {
                MediaPlayers.EXO_MEDIA_PLAYER -> {
                    mediaPlayerImpls.add {
                        ExoMediaPlayer(
                            context,
                            uiScheduler,
                            equalizerController,
                            exoPlayerMediaItemBuilder
                        )
                    }
                }
                MediaPlayers.ANDROID_MEDIA_PLAYER -> {
                    mediaPlayerImpls.add {
                        AndroidMediaPlayer(uiScheduler, equalizerController, sourceBuilder)
                    }
                }
            }
        }
        mediaPlayer = CompositeMediaPlayer(
            mediaPlayerImpls,
            1f,
            settingsRepository.soundBalance
        )
    }

    override fun prepareToPlay(source: CompositionContentSource): Completable {
        return mediaPlayer.prepareToPlay(source)
    }

    override fun stop() {
        mediaPlayer.stop()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun resume() {
        mediaPlayer.resume()
    }

    override fun seekTo(position: Long) {
        mediaPlayer.seekTo(position)
    }

    override fun setVolume(volume: Float) {
        mediaPlayer.setVolume(volume)
    }

    override fun setSoundBalance(soundBalance: SoundBalance) {
        mediaPlayer.setSoundBalance(soundBalance)
    }

    override fun getTrackPosition(): Single<Long> {
        return mediaPlayer.getTrackPosition()
    }

    override fun getDuration(): Single<Long> {
        return mediaPlayer.getDuration()
    }

    override fun setPlaybackSpeed(speed: Float) {
        mediaPlayer.setPlaybackSpeed(speed)
        currentSpeedSubject.onNext(speed)
    }

    override fun getTrackPositionObservable(): Observable<Long> {
        return mediaPlayer.getTrackPositionObservable()
    }

    override fun getPlayerEventsObservable(): Observable<MediaPlayerEvent> {
        return mediaPlayer.getPlayerEventsObservable()
    }

    override fun getCurrentPlaybackSpeedObservable(): Observable<Float> {
        return currentSpeedSubject
    }

    override fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return mediaPlayer.getSpeedChangeAvailableObservable()
    }

}