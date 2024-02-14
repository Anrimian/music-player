package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.filesync.SyncInteractor;
import com.github.anrimian.filesync.models.state.file.FileSyncState;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueData;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState;
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import io.reactivex.rxjava3.core.Observable;

public class PlayerScreenInteractor {

    private final SleepTimerInteractor sleepTimerInteractor;
    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final SyncInteractor<?, ?, Long> syncInteractor;
    private final PlayQueueRepository playQueueRepository;
    private final UiStateRepository uiStateRepository;
    private final SettingsRepository settingsRepository;
    private final MediaScannerRepository mediaScannerRepository;
    private final SystemMusicController systemMusicController;

    public PlayerScreenInteractor(SleepTimerInteractor sleepTimerInteractor,
                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                  SyncInteractor<?, ?, Long> syncInteractor,
                                  PlayQueueRepository playQueueRepository,
                                  UiStateRepository uiStateRepository,
                                  SettingsRepository settingsRepository,
                                  MediaScannerRepository mediaScannerRepository,
                                  SystemMusicController systemMusicController) {
        this.sleepTimerInteractor = sleepTimerInteractor;
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.syncInteractor = syncInteractor;
        this.playQueueRepository = playQueueRepository;
        this.uiStateRepository = uiStateRepository;
        this.settingsRepository = settingsRepository;
        this.mediaScannerRepository = mediaScannerRepository;
        this.systemMusicController = systemMusicController;
    }

    public void setPlayerPanelOpen(boolean open) {
        uiStateRepository.setPlayerPanelOpen(open);
    }

    public boolean isPlayerPanelOpen() {
        return uiStateRepository.isPlayerPanelOpen();
    }

    public void setSelectedDrawerScreen(int screenId) {
        uiStateRepository.setSelectedDrawerScreen(screenId);
    }

    public int getSelectedDrawerScreen() {
        return uiStateRepository.getSelectedDrawerScreen();
    }

    public long getSelectedPlayListScreenId() {
        return uiStateRepository.getSelectedPlayListScreenId();
    }

    public void setSelectedLibraryScreen(int screenId) {
        uiStateRepository.setSelectedLibraryScreen(screenId);
    }

    public int getSelectedLibraryScreen() {
        return uiStateRepository.getSelectedLibraryScreen();
    }

    public long getSelectedArtistScreenId() {
        return uiStateRepository.getSelectedArtistScreenId();
    }

    public long getSelectedAlbumScreenId() {
        return uiStateRepository.getSelectedAlbumScreenId();
    }

    public long getSelectedGenreScreenId() {
        return uiStateRepository.getSelectedGenreScreenId();
    }

    public void setPlayerContentPage(int position) {
        uiStateRepository.setPlayerContentPage(position);
    }

    public int getPlayerContentPage() {
        return uiStateRepository.getPlayerContentPage();
    }

    public Observable<Boolean> getCoversEnabledObservable() {
        return settingsRepository.getCoversEnabledObservable();
    }

    public Observable<Long> getSleepTimerCountDownObservable() {
        return sleepTimerInteractor.getSleepTimerCountDownObservable();
    }

    public Observable<FileScannerState> getFileScannerStateObservable() {
        return mediaScannerRepository.getFileScannerStateObservable();
    }

    public Observable<FileSyncState> getCurrentCompositionFileSyncState() {
        return libraryPlayerInteractor.getCurrentQueueItemObservable()
                .switchMap(queueItem -> {
                    PlayQueueItem item = queueItem.getPlayQueueItem();
                    if (item == null) {
                        return Observable.just(FileSyncState.NotActive.INSTANCE);
                    }
                    return syncInteractor.getFileSyncStateObservable(item.getComposition().getId());
                });
    }

    public Observable<PlayQueueData> getPlayQueueDataObservable() {
        return playQueueRepository.getPlayQueueDataObservable();
    }

    public Observable<Boolean> getPlayerScreensSwipeObservable() {
        return settingsRepository.getPlayerScreensSwipeObservable();
    }

    public Observable<VolumeState> getVolumeObservable() {
        return systemMusicController.getVolumeStateObservable();
    }

}
