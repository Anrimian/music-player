package com.github.anrimian.musicplayer.domain.models.volume

class VolumeState(val max: Int) {
    private var volume: Int = 0

    fun setVolume(volume: Int): VolumeState {
        this.volume = volume
        return this
    }

    fun getVolume() = volume
}