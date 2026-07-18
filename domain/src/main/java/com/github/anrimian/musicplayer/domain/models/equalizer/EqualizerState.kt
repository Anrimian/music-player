package com.github.anrimian.musicplayer.domain.models.equalizer

class EqualizerState(var currentPreset: Short, var bandLevels: MutableMap<Short, Short>) {

    override fun toString(): String {
        return "EqualizerState{" +
                "currentPreset=" + currentPreset +
                ", bandLevels=" + bandLevels +
                '}'
    }
}
