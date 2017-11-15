package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.TrackState;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created on 10.11.2017.
 */

public interface MusicPlayerController {

    Completable play(Composition composition);

    void stop();

    void resume();

    Observable<InternalPlayerState> getPlayerStateObservable();

    Observable<TrackState> getTrackStateObservable();
}
