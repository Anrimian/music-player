package com.github.anrimian.simplemusicplayer.domain.models.player;

/**
 * Created on 15.11.2017.
 */

public class TrackState {

    private long currentPosition;

    public TrackState(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }
}
