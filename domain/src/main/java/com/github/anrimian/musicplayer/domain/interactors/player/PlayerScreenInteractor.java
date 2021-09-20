package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor;
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import io.reactivex.rxjava3.core.Observable;

public class PlayerScreenInteractor {

    private final SleepTimerInteractor sleepTimerInteractor;
    private final UiStateRepository uiStateRepository;
    private final SettingsRepository settingsRepository;
    private final MediaScannerRepository mediaScannerRepository;

    public PlayerScreenInteractor(SleepTimerInteractor sleepTimerInteractor,
                                  UiStateRepository uiStateRepository,
                                  SettingsRepository settingsRepository,
                                  MediaScannerRepository mediaScannerRepository) {
        this.sleepTimerInteractor = sleepTimerInteractor;
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

    public int getSelectedLibraryScreen() {
        return uiStateRepository.getSelectedLibraryScreen();
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
}
