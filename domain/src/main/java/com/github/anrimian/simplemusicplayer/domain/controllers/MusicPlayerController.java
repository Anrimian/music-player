package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created on 10.11.2017.
 */

public interface MusicPlayerController {

    Observable<PlayerEvent> getEventsObservable();

    Completable prepareToPlay(CurrentComposition composition);

    void prepareToPlayIgnoreError(CurrentComposition composition);

    void stop();

    void resume();

    void pause();

    void seekTo(long position);

    Observable<InternalPlayerState> getPlayerStateObservable();

    Observable<Long> getTrackPositionObservable();
}
