package com.github.anrimian.musicplayer.domain.models.equalizer;

import java.util.Map;

public class EqualizerState {

    private short currentPreset;
    private Map<Short, Short> bendLevels;

    public EqualizerState(short currentPreset, Map<Short, Short> bendLevels) {
        this.currentPreset = currentPreset;
        this.bendLevels = bendLevels;
    }

    public short getCurrentPreset() {
        return currentPreset;
    }

    public Map<Short, Short> getBendLevels() {
        return bendLevels;
    }

    public void setCurrentPreset(short currentPreset) {
        this.currentPreset = currentPreset;
    }

    public void setBendLevels(Map<Short, Short> bendLevels) {
        this.bendLevels = bendLevels;
    }

    @Override
    public String toString() {
        return "EqualizerState{" +
                "currentPreset=" + currentPreset +
                ", bendLevels=" + bendLevels +
                '}';
    }
}
