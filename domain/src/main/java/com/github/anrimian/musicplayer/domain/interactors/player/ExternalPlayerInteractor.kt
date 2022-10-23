package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class ExternalPlayerInteractor(
    private val playerCoordinatorInteractor: PlayerCoordinatorInteractor,
    private val settingsRepository: SettingsRepository
) {

    private val playerDisposable = CompositeDisposable()
    private val trackPositionSubject = PublishSubject.create<Long>()

    private val playbackSpeedSubject = BehaviorSubject.createDefault(1f)

    private var currentSource: CompositionSource? = null

    init {
        playerDisposable.add(
            playerCoordinatorInteractor.getPlayerEventsObservable(PlayerType.EXTERNAL)
                .subscribe(this::onMusicPlayerEventReceived)
        )
    }

    fun startPlaying(source: CompositionSource) {
        currentSource = source
        setPlaybackSpeed(1f)
        playerCoordinatorInteractor.prepareToPlay(source, PlayerType.EXTERNAL, 0)
        playerCoordinatorInteractor.playAfterPrepare(PlayerType.EXTERNAL)
    }

    fun playOrPause() {
        playerCoordinatorInteractor.playOrPause(PlayerType.EXTERNAL)
    }

    fun stop() {
        playerCoordinatorInteractor.stop(PlayerType.EXTERNAL)
    }

    fun onSeekStarted() {
        playerCoordinatorInteractor.onSeekStarted(PlayerType.EXTERNAL)
    }

    fun seekTo(position: Long) {
        trackPositionSubject.onNext(position)
    }

    fun onSeekFinished(position: Long) {
        playerCoordinatorInteractor.onSeekFinished(position, PlayerType.EXTERNAL)
        trackPositionSubject.onNext(position)
    }

    fun changeExternalPlayerRepeatMode() {
        if (settingsRepository.externalPlayerRepeatMode == RepeatMode.NONE) {
            settingsRepository.externalPlayerRepeatMode = RepeatMode.REPEAT_COMPOSITION
        } else {
            settingsRepository.externalPlayerRepeatMode = RepeatMode.NONE
        }
    }

    fun setExternalPlayerRepeatMode(mode: Int) {
        //not supported
        if (mode == RepeatMode.REPEAT_PLAY_LIST) {
            return
        }
        settingsRepository.externalPlayerRepeatMode = mode
    }

    fun fastSeekForward() {
        playerCoordinatorInteractor.fastSeekForward(PlayerType.EXTERNAL).subscribe()
    }

    fun fastSeekBackward() {
        playerCoordinatorInteractor.fastSeekBackward(PlayerType.EXTERNAL).subscribe()
    }

    fun getExternalPlayerRepeatModeObservable(): Observable<Int> {
        return settingsRepository.externalPlayerRepeatModeObservable
    }

    fun setExternalPlayerKeepInBackground(enabled: Boolean) {
        settingsRepository.isExternalPlayerKeepInBackground = enabled
    }

    fun isExternalPlayerKeepInBackground() = settingsRepository.isExternalPlayerKeepInBackground

    fun setPlaybackSpeed(speed: Float) {
        playerCoordinatorInteractor.setPlaybackSpeed(speed, PlayerType.EXTERNAL)
        playbackSpeedSubject.onNext(speed)
    }

    fun getPlaybackSpeedObservable(): Observable<Float> = playbackSpeedSubject

    fun getTrackPositionObservable(): Observable<Long> {
        return playerCoordinatorInteractor.getTrackPositionObservable(PlayerType.EXTERNAL)
            .mergeWith(trackPositionSubject)
    }

    fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return playerCoordinatorInteractor.getSpeedChangeAvailableObservable()
    }

    fun getPlayerStateObservable(): Observable<PlayerState> {
        return playerCoordinatorInteractor.getPlayerStateObservable(PlayerType.EXTERNAL)
    }

    fun getIsPlayingStateObservable(): Observable<Boolean> {
        return playerCoordinatorInteractor.getIsPlayingStateObservable(PlayerType.EXTERNAL)
    }

    fun getCurrentSource() = currentSource

    private fun onMusicPlayerEventReceived(playerEvent: PlayerEvent) {
        when (playerEvent) {
            is PlayerEvent.FinishedEvent -> {
                onSeekFinished(0)
                if (settingsRepository.externalPlayerRepeatMode != RepeatMode.REPEAT_COMPOSITION) {
                    playerCoordinatorInteractor.pause(PlayerType.EXTERNAL)
                }
            }
            is PlayerEvent.ErrorEvent -> playerCoordinatorInteractor.error(
                PlayerType.EXTERNAL,
                playerEvent.throwable
            )
            else -> {}
        }
    }

}