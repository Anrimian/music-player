package com.github.anrimian.simplemusicplayer.infrastructure.service.music.models;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

/**
 * Created on 11.11.2017.
 */

public class PlayerMetaState {

    private final PlayerState state;
    private final Composition composition;
    private final long trackPosition;

    public PlayerMetaState(PlayerState state, Composition composition, long trackPosition) {
        this.state = state;
        this.composition = composition;
        this.trackPosition = trackPosition;
    }

    public PlayerMetaState(Composition composition, PlayerState state) {
        this(state, composition, 0);
    }

    public long getTrackPosition() {
        return trackPosition;
    }

    public PlayerState getState() {
        return state;
    }

    public Composition getComposition() {
        return composition;
    }

}
