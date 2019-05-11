package com.github.anrimian.musicplayer.domain.business.player;

import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;

public class MusicServiceInteractor {

    private final SettingsRepository settingsRepository;

    public MusicServiceInteractor(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Observable<MusicNotificationSetting> getNotificationSettingObservable() {
        return Observable.combineLatest(settingsRepository.getCoversEnabledObservable(),
                settingsRepository.getCoversInNotificationEnabledObservable(),
                settingsRepository.getColoredNotificationEnabledObservable(),
                settingsRepository.getCoversOnLockScreenEnabledObservable(),
                this::mapToSettingModel);
    }

    private MusicNotificationSetting mapToSettingModel(boolean coversEnabled,
                                                       boolean notificationCovers,
                                                       boolean coloredNotification,
                                                       boolean coversOnLockScreen) {
        return new MusicNotificationSetting(coversEnabled && notificationCovers,
                coloredNotification,
                coversEnabled && coversOnLockScreen);
    }
}
