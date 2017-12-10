package com.github.anrimian.simplemusicplayer.infrastructure.service.models;

/**
 * Created on 10.12.2017.
 */

public class TrackInfo {

    private int state;
    private long trackPosition;

    public TrackInfo(int state, long trackPosition) {
        this.state = state;
        this.trackPosition = trackPosition;
    }

    public int getState() {
        return state;
    }

    public long getTrackPosition() {
        return trackPosition;
    }
}
