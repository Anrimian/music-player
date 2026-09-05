package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.models.composition.content.NoReadPermissionException
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.repositories.ExternalMediaSourceRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean

class ExternalPlayerInteractor(
    private val playerCoordinatorInteractor: PlayerCoordinatorInteractor,
    private val externalMediaSourceRepository: ExternalMediaSourceRepository,
    private val settingsRepository: SettingsRepository,
    private val systemMusicController: SystemMusicController
) {

    private val playerDisposable = CompositeDisposable()
    private val trackPositionSubject = PublishSubject.create<Long>()

    private val playbackSpeedSubject = BehaviorSubject.createDefault(1f)
    private val currentSourceSubject = BehaviorSubject.create<Opt<CompositionSource>>()

    private var currentFileRef: FileReference? = null
    private val isAnySourcePrepared = AtomicBoolean(false)
    private var activePreparationCompletable: Completable? = null

    private val resetSignalSubject = PublishSubject.create<Any>()

    init {
        playerCoordinatorInteractor.registerCleanupCallback(PlayerType.EXTERNAL, ::clearState)
    }

    fun startPlaying(fileRef: FileReference): Completable {
        isAnySourcePrepared.set(false)
        activePreparationCompletable = null

        this.currentFileRef = fileRef
        return ensureSourceReady().doOnComplete {
            playerCoordinatorInteractor.playAfterPrepare(PlayerType.EXTERNAL)
        }
    }

    @Suppress("CheckResult")
    fun play(delay: Long) {
        ensureSourceReady().subscribe(
            { playerCoordinatorInteractor.play(PlayerType.EXTERNAL, delay) },
            {}
        )
    }

    @Suppress("CheckResult")
    fun playOrPause() {
        ensureSourceReady().subscribe(
            { playerCoordinatorInteractor.playOrPause(PlayerType.EXTERNAL) },
            {}
        )
    }

    fun stop() {
        playerCoordinatorInteractor.stop(PlayerType.EXTERNAL)
    }

    fun reset() {
        playerCoordinatorInteractor.reset(PlayerType.EXTERNAL, true)
        resetSignalSubject.onNext(Constants.TRIGGER)
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
        // unsupported
        if (mode == RepeatMode.REPEAT_PLAY_QUEUE) {
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

    fun getCurrentSourceObservable(): Observable<Opt<CompositionSource>> = currentSourceSubject

    fun getVolumeObservable(): Observable<VolumeState> {
        return systemMusicController.getVolumeStateObservable()
    }

    fun getResetSignalObservable(): Observable<Any> = resetSignalSubject

    private fun ensureSourceReady(): Completable {
        return Completable.defer {
            val fileRef = currentFileRef ?: return@defer Completable.error(Exception())

            if (activePreparationCompletable != null) {
                return@defer activePreparationCompletable
            }
            if (isAnySourcePrepared.get()) {
                return@defer Completable.complete()
            }
            val completable = externalMediaSourceRepository.getCompositionSource(fileRef)
                .doOnSuccess { source ->
                    subscribeOnPlayerEvents()
                    currentSourceSubject.onNext(Opt(source))
                    setPlaybackSpeed(1f)
                    playerCoordinatorInteractor.prepareToPlay(source, PlayerType.EXTERNAL, 0)
                    isAnySourcePrepared.set(true)
                }
                .doOnError { t ->
                    isAnySourcePrepared.set(false)
                    playerCoordinatorInteractor.pause(PlayerType.EXTERNAL)
                    currentSourceSubject.onNext(Opt())
                }
                .doFinally { activePreparationCompletable = null }
                .ignoreElement()
                .cache()
            activePreparationCompletable = completable
            return@defer completable
        }
    }

    private fun subscribeOnPlayerEvents() {
        if (playerDisposable.size() != 0) {
            return
        }
        playerDisposable.add(
            playerCoordinatorInteractor.getPlayerEventsObservable(PlayerType.EXTERNAL)
                .subscribe(this::onMusicPlayerEventReceived)
        )
        playerDisposable.add(
            playerCoordinatorInteractor.getPlayerStateObservable(PlayerType.EXTERNAL)
                .subscribe(this::onPlayerStateChanged)
        )
    }

    private fun onMusicPlayerEventReceived(playerEvent: PlayerEvent) {
        when (playerEvent) {
            is PlayerEvent.FinishedEvent -> {
                if (settingsRepository.externalPlayerRepeatMode != RepeatMode.REPEAT_COMPOSITION) {
                    playerCoordinatorInteractor.pause(PlayerType.EXTERNAL)
                }
                onSeekFinished(0)
            }
            is PlayerEvent.ErrorEvent -> {
                val throwable = playerEvent.throwable
                if (throwable is NoReadPermissionException) {
                    rePrepareSource()
                } else {
                    playerCoordinatorInteractor.error(PlayerType.EXTERNAL, playerEvent.throwable)
                }
            }
            else -> {}
        }
    }

    private fun rePrepareSource() {
        val fileRef = currentFileRef ?: return
        externalMediaSourceRepository.getCompositionSource(fileRef)
            .flatMap { source ->
                currentSourceSubject.onNext(Opt(source))
                playerCoordinatorInteractor.getActualTrackPosition(PlayerType.EXTERNAL)
                    .doOnSuccess { position ->
                        playerCoordinatorInteractor.prepareToPlay(source, PlayerType.EXTERNAL, position)
                    }
            }
            .doOnError { t -> playerCoordinatorInteractor.error(PlayerType.EXTERNAL, t) }
            .onErrorComplete()
            .subscribe()
    }

    private fun onPlayerStateChanged(playerState: PlayerState) {
        if (playerState == PlayerState.STOP) {
            trackPositionSubject.onNext(0)
        }
    }

    private fun clearState() {
        activePreparationCompletable = null
        playerDisposable.clear()
        externalMediaSourceRepository.deleteAllData()
    }

}