package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.equalizer.EqInitializationState;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class EqualizerInteractor {

    private final EqualizerRepository equalizerRepository;

    public EqualizerInteractor(EqualizerRepository equalizerRepository) {
        this.equalizerRepository = equalizerRepository;
    }

    public Single<EqualizerConfig> getEqualizerConfig() {
        return equalizerRepository.getEqualizerConfig();
    }

    public Observable<EqualizerState> getEqualizerStateObservable() {
        return equalizerRepository.getEqualizerStateObservable();
    }

    public void setBandLevel(short bandNumber, short level) {
        equalizerRepository.setBandLevel(bandNumber, level);
    }

    public void saveBandLevel() {
        equalizerRepository.saveBandLevel();
    }

    public void setPreset(Preset preset) {
        equalizerRepository.setPreset(preset);
    }

    public Observable<EqInitializationState> getEqInitializationState() {
        return equalizerRepository.getEqInitializationState();
    }

    public void tryToReattachEqualizer() {
        equalizerRepository.tryToReattachEqualizer();
    }
}
