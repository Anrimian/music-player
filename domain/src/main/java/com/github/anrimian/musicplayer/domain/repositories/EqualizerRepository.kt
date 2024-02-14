package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.equalizer.EqInitializationState
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface EqualizerRepository {

    fun getEqualizerConfig(): Single<EqualizerConfig>

    fun getEqualizerStateObservable(): Observable<EqualizerState>

    fun setBandLevel(bandNumber: Short, level: Short)

    fun saveBandLevel()

    fun setPreset(preset: Preset)

    fun getEqInitializationState(): Observable<EqInitializationState>

    fun tryToReattachEqualizer()

}