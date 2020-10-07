package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerInfo;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

import io.reactivex.rxjava3.core.Observable;

public class EqualizerInteractor {

    private final EqualizerRepository equalizerRepository;

    public EqualizerInteractor(EqualizerRepository equalizerRepository) {
        this.equalizerRepository = equalizerRepository;
    }

    public Observable<EqualizerInfo> getEqualizerInfoObservable() {
        return equalizerRepository.getEqualizerInfoObservable();
    }

    public void setBandLevel(short bandNumber, short level) {
        equalizerRepository.setBandLevel(bandNumber, level);
    }

    public void setPreset(Preset preset) {
        equalizerRepository.setPreset(preset);
    }

}
