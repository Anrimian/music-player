package com.github.anrimian.musicplayer.domain.models.equalizer;

import java.util.Map;

public class EqualizerState {

    private final short currentPreset;
    private final Map<Short, Short> bendLevels;

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
}
