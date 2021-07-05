package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.rxjava3.core.Observable;

import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.EXTERNAL;
import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.LIBRARY;

public class MusicServiceInteractor {

    private final PlayerCoordinatorInteractor playerCoordinatorInteractor;
    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final ExternalPlayerInteractor externalPlayerInteractor;
    private final SettingsRepository settingsRepository;

    public MusicServiceInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor,
                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                  ExternalPlayerInteractor externalPlayerInteractor,
                                  SettingsRepository settingsRepository) {
        this.playerCoordinatorInteractor = playerCoordinatorInteractor;
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.externalPlayerInteractor = externalPlayerInteractor;
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
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.setRepeatMode(appRepeatMode);
        } else {
            externalPlayerInteractor.setExternalPlayerRepeatMode(appRepeatMode);
        }
    }

    public void changeRepeatMode() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.changeRepeatMode();
        } else {
            externalPlayerInteractor.changeExternalPlayerRepeatMode();
        }
    }

    public void setRandomPlayingEnabled(boolean isEnabled) {
        libraryPlayerInteractor.setRandomPlayingEnabled(isEnabled);
    }

    public void setPlaybackSpeed(float speed) {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.setPlaybackSpeed(speed);
            return;
        }
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            externalPlayerInteractor.setPlaybackSpeed(speed);
        }
    }

    public Observable<Integer> getRepeatModeObservable() {
        return playerCoordinatorInteractor.getActivePlayerTypeObservable()
                .switchMap(playerType -> {
                    switch (playerType) {
                        case LIBRARY: {
                            return libraryPlayerInteractor.getRepeatModeObservable();
                        }
                        case EXTERNAL: {
                            return externalPlayerInteractor.getExternalPlayerRepeatModeObservable();
                        }
                        default: throw new IllegalStateException();
                    }
                });
    }

    public Observable<Boolean> getRandomModeObservable() {
        return playerCoordinatorInteractor.getActivePlayerTypeObservable()
                .switchMap(playerType -> {
                    switch (playerType) {
                        case LIBRARY: {
                            return libraryPlayerInteractor.getRandomPlayingObservable();
                        }
                        case EXTERNAL: {
                            return Observable.fromCallable(() -> false);
                        }
                        default: throw new IllegalStateException();
                    }
                });
    }

    public Observable<MusicNotificationSetting> getNotificationSettingObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                getColoredNotificationEnabledObservable(),
                getNotificationCoverStubEnabledObservable(),
                getCoversOnLockScreenEnabledObservable(),
                MusicNotificationSetting::new);
    }

    public MusicNotificationSetting getNotificationSettings() {
        boolean coversEnabled = settingsRepository.isCoversEnabled();
        boolean coversInNotification = coversEnabled && settingsRepository.isCoversInNotificationEnabled();
        boolean coloredNotification = settingsRepository.isColoredNotificationEnabled();
        boolean showNotificationCoverStub = settingsRepository.isNotificationCoverStubEnabled();
        boolean coversOnLockScreen = settingsRepository.isCoversOnLockScreenEnabled();
        return new MusicNotificationSetting(
                coversInNotification,
                coversInNotification && coloredNotification,
                coversInNotification && showNotificationCoverStub,
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

    private Observable<Boolean> getNotificationCoverStubEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getNotificationCoverStubEnabledObservable(),
                (coversInNotification, showNotificationCoverStub) -> coversInNotification && showNotificationCoverStub);
    }

    private Observable<Boolean> getCoversOnLockScreenEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getCoversOnLockScreenEnabledObservable(),
                (coversInNotification, coversOnLockScreen) -> coversInNotification && coversOnLockScreen);
    }
}
