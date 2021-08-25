package com.github.anrimian.musicplayer.data.repositories.equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqInitializationState;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class EqualizerRepositoryImpl implements EqualizerRepository {

    private final InternalEqualizer internalEqualizer;

    public EqualizerRepositoryImpl(InternalEqualizer internalEqualizer) {
        this.internalEqualizer = internalEqualizer;
    }

    @Override
    public Single<EqualizerConfig> getEqualizerConfig() {
        return internalEqualizer.getEqualizerConfig();
    }

    @Override
    public Observable<EqualizerState> getEqualizerStateObservable() {
        return internalEqualizer.getEqualizerStateObservable();
    }

    @Override
    public void setBandLevel(short bandNumber, short level) {
        internalEqualizer.setBandLevel(bandNumber, level);
    }

    @Override
    public void saveBandLevel() {
        internalEqualizer.saveBandLevel();
    }

    @Override
    public void setPreset(Preset preset) {
        internalEqualizer.setPreset(preset);
    }

    @Override
    public Observable<EqInitializationState> getEqInitializationState() {
        return internalEqualizer.getEqInitializationState();
    }

    @Override
    public void tryToReattachEqualizer() {
        internalEqualizer.tryToReattachEqualizer();
    }

}
