package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class EqualizerInteractor {

    private final EqualizerRepository equalizerRepository;

    public EqualizerInteractor(EqualizerRepository equalizerRepository) {
        this.equalizerRepository = equalizerRepository;
    }

    public Single<List<Band>> getBands() {
        return equalizerRepository.getBands();
    }

    public void setBandLevel(short bandNumber, short level) {
        equalizerRepository.setBandLevel(bandNumber, level);
    }

}
