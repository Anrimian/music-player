package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;

/**
 * Created on 10.11.2017.
 */

public interface MusicPlayerController {

    Observable<PlayerEvent> getEventsObservable();

    void prepareToPlay(Composition composition, long startPosition);

    void stop();

    void resume();

    void pause();

    void seekTo(long position);

    Observable<Long> getTrackPositionObservable();
}
