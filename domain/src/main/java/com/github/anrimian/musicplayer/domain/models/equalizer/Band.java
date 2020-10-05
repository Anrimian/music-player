package com.github.anrimian.musicplayer.domain.models.equalizer;

public class Band {

    private final short bandNumber;
    private final int[] frequencyRange;
    private final int centerFreq;
    private final short currentRange;

    public Band(short bandNumber,
                int[] frequencyRange,
                int centerFreq,
                short currentRange) {
        this.bandNumber = bandNumber;
        this.frequencyRange = frequencyRange;
        this.centerFreq = centerFreq;
        this.currentRange = currentRange;
    }

    public int getCenterFreq() {
        return centerFreq;
    }

    public short getBandNumber() {
        return bandNumber;
    }

    public int[] getFrequencyRange() {
        return frequencyRange;
    }

    public short getCurrentRange() {
        return currentRange;
    }
}
