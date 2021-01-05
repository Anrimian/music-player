package com.github.anrimian.musicplayer.domain.models.equalizer;

public class  Band {

    private final short bandNumber;
    private final int centerFreq;

    public Band(short bandNumber, int centerFreq) {
        this.bandNumber = bandNumber;
        this.centerFreq = centerFreq;
    }

    public int getCenterFreq() {
        return centerFreq;
    }

    public short getBandNumber() {
        return bandNumber;
    }

}
