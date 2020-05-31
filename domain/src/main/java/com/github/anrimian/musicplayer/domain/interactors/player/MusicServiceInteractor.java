package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.LIBRARY;

public class MusicServiceInteractor {

    private final PlayerCoordinatorInteractor playerCoordinatorInteractor;
    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final SettingsRepository settingsRepository;

    public MusicServiceInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor,
                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                  SettingsRepository settingsRepository) {
        this.playerCoordinatorInteractor = playerCoordinatorInteractor;
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.settingsRepository = settingsRepository;
    }

    public void skipToNext() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.skipToNext();
        }
    }

    public void skipToPrevious() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.skipToPrevious();
        }
    }

    public void setRepeatMode(int appRepeatMode) {
        libraryPlayerInteractor.setRepeatMode(appRepeatMode);
    }

    public void setRandomPlayingEnabled(boolean isEnabled) {
        libraryPlayerInteractor.setRandomPlayingEnabled(isEnabled);
    }

    public Observable<MusicNotificationSetting> getNotificationSettingObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                getColoredNotificationEnabledObservable(),
                getCoversOnLockScreenEnabledObservable(),
                MusicNotificationSetting::new);
    }

    public MusicNotificationSetting getNotificationSettings() {
        boolean coversEnabled = settingsRepository.isCoversEnabled();
        boolean coversInNotification = coversEnabled && settingsRepository.isCoversInNotificationEnabled();
        boolean coloredNotification = settingsRepository.isColoredNotificationEnabled();
        boolean coversOnLockScreen = settingsRepository.isCoversOnLockScreenEnabled();
        return new MusicNotificationSetting(
                coversInNotification,
                coversInNotification && coloredNotification,
                coversInNotification && coversOnLockScreen
        );
    }

    private Observable<Boolean> getCoversInNotificationEnabledObservable() {
        return Observable.combineLatest(settingsRepository.getCoversEnabledObservable(),
                settingsRepository.getCoversInNotificationEnabledObservable(),
                (coversEnabled, coversInNotification) -> coversEnabled && coversInNotification);
    }

    private Observable<Boolean> getColoredNotificationEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getColoredNotificationEnabledObservable(),
                (coversInNotification, coloredNotification) -> coversInNotification && coloredNotification);
    }

    private Observable<Boolean> getCoversOnLockScreenEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getCoversOnLockScreenEnabledObservable(),
                (coversInNotification, coversOnLockScreen) -> coversInNotification && coversOnLockScreen);
    }
}
