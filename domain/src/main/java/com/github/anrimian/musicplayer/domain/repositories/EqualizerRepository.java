package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerInfo;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;

import io.reactivex.rxjava3.core.Single;

public interface EqualizerRepository {

    Single<EqualizerInfo> getEqualizerInfo();

    void setBandLevel(short bandNumber, short level);

    void setPreset(Preset preset);
}
