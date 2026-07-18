package com.github.anrimian.musicplayer.domain.models.equalizer

class Preset(val number: Short, val presetName: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Preset) return false

        if (number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        return number.toInt()
    }

    override fun toString(): String {
        return presetName
    }

}
