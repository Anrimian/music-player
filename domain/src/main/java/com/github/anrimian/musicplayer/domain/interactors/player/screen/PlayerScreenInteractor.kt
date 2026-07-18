package com.github.anrimian.musicplayer.domain.interactors.player.screen

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.fsync.models.Optional
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor
import com.github.anrimian.musicplayer.domain.interactors.storage.StorageScannerInteractor
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Observable

open class PlayerScreenInteractor(
    private val sleepTimerInteractor: SleepTimerInteractor,
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val syncInteractor: SyncInteractor<*, *, Long>,
    private val playQueueRepository: PlayQueueRepository,
    private val uiStateRepository: UiStateRepository,
    private val settingsRepository: SettingsRepository,
    private val storageScannerInteractor: StorageScannerInteractor,
    private val libraryRepository: LibraryRepository,
    private val systemMusicController: SystemMusicController
) {

    fun setPlayerPanelOpen(open: Boolean) = uiStateRepository.setPlayerPanelOpen(open)

    fun isPlayerPanelOpen() = uiStateRepository.isPlayerPanelOpen()

    fun setSelectedDrawerScreen(screenId: Int) = uiStateRepository.setSelectedDrawerScreen(screenId)

    fun getSelectedDrawerScreen() = uiStateRepository.getSelectedDrawerScreen()

    fun setSelectedLibraryScreen(screenId: Int) = uiStateRepository.setSelectedLibraryScreen(screenId)

    fun getSelectedLibraryScreen() = uiStateRepository.getSelectedLibraryScreen()

    fun getSelectedPlayListScreenId() = uiStateRepository.getSelectedPlayListScreenId()

    fun getSelectedArtistScreenId() = uiStateRepository.getSelectedArtistScreenId()

    fun getSelectedAlbumScreenId() = uiStateRepository.getSelectedAlbumScreenId()

    fun getSelectedGenreScreenId() = uiStateRepository.getSelectedGenreScreenId()

    fun setPlayerContentPage(position: Int) = uiStateRepository.setPlayerContentPage(position)

    fun getPlayerContentPage() = uiStateRepository.getPlayerContentPage()

    fun getCoversEnabledObservable() = settingsRepository.getCoversEnabledObservable()

    fun getSleepTimerCountDownObservable() = sleepTimerInteractor.getSleepTimerCountDownObservable()

    fun getCurrentActionsObservable(): Observable<CurrentAction> {
        return storageScannerInteractor.getFileScannerStateObservable()
            .switchMap { scannerState ->
                if (scannerState is Running) {
                    Observable.just(ScannerRunning(scannerState.composition))
                } else {
                    libraryRepository.getMissingCompositionsCountObservable()
                        .map { count -> if (count > 0) MissingCompositions(count) else NoAction }
                }
            }
    }

    fun getCurrentCompositionFileSyncState(): Observable<Optional<FileSyncState>> {
        return libraryPlayerInteractor.getCurrentQueueItemObservable()
            .switchMap { queueItemEvent ->
                queueItemEvent.playQueueItem?.let {
                    syncInteractor.getFileSyncStateObservable(it.id)
                } ?: Observable.just(Optional())
            }
    }

    open fun getActionStateObservable(): Observable<ActionState> {
        return libraryRepository.getMissingCompositionsCountObservable()
            .map { count -> if (count > 0) ActionState.ACTION_REQUIRED else ActionState.NO_STATE }
    }

    fun getPlayQueueDataObservable() = playQueueRepository.getPlayQueueDataObservable()

    fun getPlayerScreensSwipeObservable() = settingsRepository.getPlayerScreensSwipeObservable()

    fun getVolumeObservable() = systemMusicController.getVolumeStateObservable()

}
