package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

public class EqualizerInteractor {

    private final EqualizerRepository equalizerRepository;

    public EqualizerInteractor(EqualizerRepository equalizerRepository) {
        this.equalizerRepository = equalizerRepository;
    }

}
