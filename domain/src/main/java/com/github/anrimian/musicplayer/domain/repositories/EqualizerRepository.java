package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.equalizer.Band;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface EqualizerRepository {

    Single<List<Band>> getBands();

    void setBandLevel(short bandNumber, short level);

}
