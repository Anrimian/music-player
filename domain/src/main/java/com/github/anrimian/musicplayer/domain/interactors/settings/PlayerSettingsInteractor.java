package com.github.anrimian.musicplayer.domain.interactors.settings;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.rxjava3.core.Observable;

public class PlayerSettingsInteractor {

    private final SettingsRepository settingsRepository;

    public PlayerSettingsInteractor(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public boolean isDecreaseVolumeOnAudioFocusLossEnabled() {
        return settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled();
    }

    public void setDecreaseVolumeOnAudioFocusLossEnabled(boolean enabled) {
        settingsRepository.setDecreaseVolumeOnAudioFocusLossEnabled(enabled);
    }

    public boolean isPauseOnAudioFocusLossEnabled() {
        return settingsRepository.isPauseOnAudioFocusLossEnabled();
    }

    public void setPauseOnZeroVolumeLevelEnabled(boolean enabled) {
        settingsRepository.setPauseOnZeroVolumeLevelEnabled(enabled);
    }

    public boolean isPauseOnZeroVolumeLevelEnabled() {
        return settingsRepository.isPauseOnZeroVolumeLevelEnabled();
    }

    public void setPauseOnAudioFocusLossEnabled(boolean enabled) {
        settingsRepository.setPauseOnAudioFocusLossEnabled(enabled);
    }

    public Observable<Integer> getSelectedEqualizerTypeObservable() {
        return settingsRepository.getSelectedEqualizerTypeObservable();
    }
}
