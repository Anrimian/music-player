package com.github.anrimian.musicplayer.data.repositories.equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class EqualizerRepositoryImpl implements EqualizerRepository {

    private final InternalEqualizer internalEqualizer;

    public EqualizerRepositoryImpl(InternalEqualizer internalEqualizer) {
        this.internalEqualizer = internalEqualizer;
    }

    @Override
    public Single<List<Band>> getBands() {
        return internalEqualizer.getBands();
    }

    @Override
    public void setBandLevel(short bandNumber, short level) {
        internalEqualizer.setBandLevel(bandNumber, level);
    }

}
