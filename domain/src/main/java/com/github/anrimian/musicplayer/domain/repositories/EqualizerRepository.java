package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.equalizer.EqInitializationState;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface EqualizerRepository {

    Single<EqualizerConfig> getEqualizerConfig();

    Observable<EqualizerState> getEqualizerStateObservable();

    void setBandLevel(short bandNumber, short level);

    void saveBandLevel();

    void setPreset(Preset preset);

    Observable<EqInitializationState> getEqInitializationState();

    void tryToReattachEqualizer();
}
