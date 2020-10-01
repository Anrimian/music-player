package com.github.anrimian.musicplayer.domain.models.equalizer;

public class Band {

    private final short bandNumber;
    private final int[] frequencyRange;
    private final short[] levelRange;
    private final short currentRange;

    public Band(short bandNumber, int[] frequencyRange, short[] levelRange, short currentRange) {
        this.bandNumber = bandNumber;
        this.frequencyRange = frequencyRange;
        this.levelRange = levelRange;
        this.currentRange = currentRange;
    }

    public short getBandNumber() {
        return bandNumber;
    }

    public int[] getFrequencyRange() {
        return frequencyRange;
    }

    public short[] getLevelRange() {
        return levelRange;
    }

    public short getCurrentRange() {
        return currentRange;
    }
}
