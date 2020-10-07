package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerInfo;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;

import io.reactivex.rxjava3.core.Observable;

public interface EqualizerRepository {

    Observable<EqualizerInfo> getEqualizerInfoObservable();

    void setBandLevel(short bandNumber, short level);

    void setPreset(Preset preset);
}
