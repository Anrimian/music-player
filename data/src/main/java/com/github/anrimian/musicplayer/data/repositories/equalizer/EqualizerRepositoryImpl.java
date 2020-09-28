package com.github.anrimian.musicplayer.data.repositories.equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

public class EqualizerRepositoryImpl implements EqualizerRepository {

    private final InternalEqualizer internalEqualizer;

    public EqualizerRepositoryImpl(InternalEqualizer internalEqualizer) {
        this.internalEqualizer = internalEqualizer;
    }
}
