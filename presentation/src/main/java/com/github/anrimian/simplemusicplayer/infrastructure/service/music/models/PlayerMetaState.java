package com.github.anrimian.simplemusicplayer.infrastructure.service.music.models;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

/**
 * Created on 11.11.2017.
 */

public class PlayerMetaState {

    private final PlayerState state;
    private final Composition composition;
    private final long trackPosition;

    public PlayerMetaState(PlayerState state, CompositionEvent compositionEvent, long trackPosition) {
        this.state = state;
        this.composition = compositionEvent.getComposition();
        this.trackPosition = trackPosition;
    }

    public PlayerMetaState(CompositionEvent compositionEvent, PlayerState state) {
        this(state, compositionEvent, 0);
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

    @Override
    public String toString() {
        return "PlayerMetaState{" +
                "state=" + state +
                ", composition=" + composition +
                ", trackPosition=" + trackPosition +
                '}';
    }
}
