package com.github.anrimian.musicplayer.domain.models.equalizer;

public class Preset {

    private final short number;
    private final String presetName;

    public Preset(short number, String presetName) {
        this.number = number;
        this.presetName = presetName;
    }

    public short getNumber() {
        return number;
    }

    public String getPresetName() {
        return presetName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Preset preset = (Preset) o;

        return number == preset.number;
    }

    @Override
    public int hashCode() {
        return (int) number;
    }

    @Override
    public String toString() {
        return presetName;
    }
}
