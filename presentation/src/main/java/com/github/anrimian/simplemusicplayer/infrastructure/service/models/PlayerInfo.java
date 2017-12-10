package com.github.anrimian.simplemusicplayer.infrastructure.service.models;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

/**
 * Created on 11.11.2017.
 */

public class PlayerInfo {

    private int state;
    private Composition composition;

    public PlayerInfo(int state, Composition composition) {
        this.state = state;
        this.composition = composition;
    }

    public int getState() {
        return state;
    }

    public Composition getComposition() {
        return composition;
    }

}
