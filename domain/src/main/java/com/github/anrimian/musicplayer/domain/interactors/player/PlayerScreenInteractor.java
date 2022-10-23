package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.filesync.SyncInteractor;
import com.github.anrimian.filesync.models.state.file.FormattedFileSyncState;
import com.github.anrimian.filesync.models.state.file.NotActive;
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import io.reactivex.rxjava3.core.Observable;

public class PlayerScreenInteractor {

    private final SleepTimerInteractor sleepTimerInteractor;
    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final SyncInteractor<?, ?, Long> syncInteractor;
    private final UiStateRepository uiStateRepository;
    private final SettingsRepository settingsRepository;
    private final MediaScannerRepository mediaScannerRepository;

    public PlayerScreenInteractor(SleepTimerInteractor sleepTimerInteractor,
                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                  SyncInteractor<?, ?, Long> syncInteractor,
                                  UiStateRepository uiStateRepository,
                                  SettingsRepository settingsRepository,
                                  MediaScannerRepository mediaScannerRepository) {
        this.sleepTimerInteractor = sleepTimerInteractor;
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.syncInteractor = syncInteractor;
        this.uiStateRepository = uiStateRepository;
        this.settingsRepository = settingsRepository;
        this.mediaScannerRepository = mediaScannerRepository;
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

    public Observable<FormattedFileSyncState> getCurrentCompositionFileSyncState() {
        FormattedFileSyncState syncState = new FormattedFileSyncState();
        return libraryPlayerInteractor.getCurrentCompositionObservable()
                .switchMap(currentComposition -> {
                    Composition composition = currentComposition.getComposition();
                    if (composition == null) {
                        return Observable.just(syncState.set(NotActive.INSTANCE, false));
                    }
                    return syncInteractor.getFileSyncStateObservable(composition.getId())
                            .map(state -> syncState.set(state,
                                    CompositionHelper.isCompositionFileRemote(composition)
                            ));
                });
    }
}
