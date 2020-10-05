package com.github.anrimian.musicplayer.domain.models.equalizer;

import java.util.List;

public class EqualizerInfo {

    private final short[] bandLevelRange;
    private final List<Band> bands;
    private final List<Preset> presets;
    private final Preset currentPreset;

    public EqualizerInfo(short[] bandLevelRange,
                         List<Band> bands,
                         List<Preset> presets,
                         Preset currentPreset) {
        this.bandLevelRange = bandLevelRange;
        this.bands = bands;
        this.presets = presets;
        this.currentPreset = currentPreset;
    }

    public short[] getBandLevelRange() {
        return bandLevelRange;
    }

    public List<Band> getBands() {
        return bands;
    }

    public List<Preset> getPresets() {
        return presets;
    }

    public Preset getCurrentPreset() {
        return currentPreset;
    }
}
