package com.github.anrimian.musicplayer.infrastructure.service.music.models.mappers;

import android.support.v4.media.session.PlaybackStateCompat;

import com.github.anrimian.musicplayer.domain.models.player.PlayerState;

import java.util.HashMap;

/**
 * Created on 10.12.2017.
 */

public class PlayerStateMapper {

    private static HashMap<PlayerState, Integer> stateMap = new HashMap<>();

    static {
        stateMap.put(PlayerState.IDLE, PlaybackStateCompat.STATE_NONE);
        stateMap.put(PlayerState.LOADING, PlaybackStateCompat.STATE_CONNECTING);
        stateMap.put(PlayerState.PAUSE, PlaybackStateCompat.STATE_PAUSED);
        stateMap.put(PlayerState.PLAY, PlaybackStateCompat.STATE_PLAYING);
        stateMap.put(PlayerState.STOP, PlaybackStateCompat.STATE_STOPPED);
    }

    public static int toMediaState(PlayerState playerState) {
        return stateMap.get(playerState);
    }
}
