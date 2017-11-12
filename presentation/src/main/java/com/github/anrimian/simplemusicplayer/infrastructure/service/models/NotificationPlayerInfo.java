package com.github.anrimian.simplemusicplayer.infrastructure.service.models;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

/**
 * Created on 11.11.2017.
 */

public class NotificationPlayerInfo {

    private PlayerState state;
    private Composition composition;

    public NotificationPlayerInfo(PlayerState state, Composition composition) {
        this.state = state;
        this.composition = composition;
    }

    public PlayerState getState() {
        return state;
    }

    public Composition getComposition() {
        return composition;
    }

}
