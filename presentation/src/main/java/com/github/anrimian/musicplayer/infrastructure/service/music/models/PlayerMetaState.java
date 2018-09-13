package com.github.anrimian.musicplayer.infrastructure.service.music.models;

import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;

/**
 * Created on 11.11.2017.
 */

public class PlayerMetaState {

    private final PlayerState state;
    private final PlayQueueItem composition;
    private final long trackPosition;

    public PlayerMetaState(PlayerState state, PlayQueueEvent playQueueEvent, long trackPosition) {
        this.state = state;
        this.composition = playQueueEvent.getPlayQueueItem();
        this.trackPosition = trackPosition;
    }

    public PlayerMetaState(PlayQueueEvent playQueueEvent, PlayerState state) {
        this(state, playQueueEvent, 0);
    }

    public long getTrackPosition() {
        return trackPosition;
    }

    public PlayerState getState() {
        return state;
    }

    public PlayQueueItem getQueueItem() {
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
