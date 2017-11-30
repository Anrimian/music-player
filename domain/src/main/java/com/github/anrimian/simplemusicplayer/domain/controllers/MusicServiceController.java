package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created on 10.11.2017.
 */

public interface MusicServiceController {

    @Deprecated
    void start();

    void startPlaying(List<Composition> compositions);

    void play();

    void pause();

    void stop();

    void skipToPrevious();

    void skipToNext();

    Observable<PlayerState> getPlayerStateObservable();

    Observable<Composition> getCurrentCompositionObservable();

    Observable<List<Composition>> getCurrentPlayListObservable();

    Observable<Long> getTrackPositionObservable();

    boolean isInfinitePlayingEnabled();

    boolean isRandomPlayingEnabled();

    void setRandomPlayingEnabled(boolean enabled);

    void setInfinitePlayingEnabled(boolean enabled);

}
