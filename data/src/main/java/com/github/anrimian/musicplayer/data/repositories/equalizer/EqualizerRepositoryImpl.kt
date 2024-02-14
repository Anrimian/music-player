package com.github.anrimian.musicplayer.data.repositories.equalizer

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer
import com.github.anrimian.musicplayer.domain.models.equalizer.EqInitializationState
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class EqualizerRepositoryImpl(
    private val internalEqualizer: InternalEqualizer
) : EqualizerRepository {

    override fun getEqualizerConfig(): Single<EqualizerConfig> {
        return internalEqualizer.getEqualizerConfig()
    }

    override fun getEqualizerStateObservable(): Observable<EqualizerState> {
        return internalEqualizer.getEqualizerStateObservable()
    }

    override fun setBandLevel(bandNumber: Short, level: Short) {
        internalEqualizer.setBandLevel(bandNumber, level)
    }

    override fun saveBandLevel() {
        internalEqualizer.saveBandLevel()
    }

    override fun setPreset(preset: Preset) {
        internalEqualizer.setPreset(preset)
    }

    override fun getEqInitializationState(): Observable<EqInitializationState> {
        return internalEqualizer.getEqInitializationState()
    }

    override fun tryToReattachEqualizer() {
        internalEqualizer.tryToReattachEqualizer()
    }

}