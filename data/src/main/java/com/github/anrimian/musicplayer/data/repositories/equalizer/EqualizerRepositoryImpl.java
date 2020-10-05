package com.github.anrimian.musicplayer.data.repositories.equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerInfo;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

import io.reactivex.rxjava3.core.Single;

public class EqualizerRepositoryImpl implements EqualizerRepository {

    private final InternalEqualizer internalEqualizer;

    public EqualizerRepositoryImpl(InternalEqualizer internalEqualizer) {
        this.internalEqualizer = internalEqualizer;
    }

    @Override
    public Single<EqualizerInfo> getEqualizerInfo() {
        return internalEqualizer.getEqualizerInfo();
    }

    @Override
    public void setBandLevel(short bandNumber, short level) {
        internalEqualizer.setBandLevel(bandNumber, level);
    }

    @Override
    public void setPreset(Preset preset) {
        internalEqualizer.setPreset(preset);
    }

}
