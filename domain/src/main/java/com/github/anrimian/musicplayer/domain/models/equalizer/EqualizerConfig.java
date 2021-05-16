package com.github.anrimian.musicplayer.domain.models.equalizer;

import java.util.List;

public class EqualizerConfig {

    private final short lowestBandRange;
    private final short highestBandRange;
    private final List<Band> bands;
    private final List<Preset> presets;

    public EqualizerConfig(
            short lowestBandRange,
            short highestBandRange,
            List<Band> bands,
            List<Preset> presets) {
        this.lowestBandRange = lowestBandRange;
        this.highestBandRange = highestBandRange;
        this.bands = bands;
        this.presets = presets;
    }

    public short getLowestBandRange() {
        return lowestBandRange;
    }

    public short getHighestBandRange() {
        return highestBandRange;
    }

    public List<Band> getBands() {
        return bands;
    }

    public List<Preset> getPresets() {
        return presets;
    }

}
