package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.models.equalizer.Preset
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository

class EqualizerInteractor(private val equalizerRepository: EqualizerRepository) {

    fun getEqualizerConfig() = equalizerRepository.getEqualizerConfig()

    fun getEqualizerStateObservable() = equalizerRepository.getEqualizerStateObservable()

    fun setBandLevel(bandNumber: Short, level: Short) {
        equalizerRepository.setBandLevel(bandNumber, level)
    }

    fun saveBandLevel() {
        equalizerRepository.saveBandLevel()
    }

    fun setPreset(preset: Preset?) {
        equalizerRepository.setPreset(preset!!)
    }

    fun getEqInitializationState() = equalizerRepository.getEqInitializationState()

    fun tryToReattachEqualizer() {
        equalizerRepository.tryToReattachEqualizer()
    }

}