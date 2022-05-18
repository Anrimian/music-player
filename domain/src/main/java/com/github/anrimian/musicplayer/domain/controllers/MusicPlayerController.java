package com.github.anrimian.musicplayer.domain.controllers;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * Created on 10.11.2017.
 */

public interface MusicPlayerController {

    Observable<PlayerEvent> getEventsObservable();

    void prepareToPlay(CompositionSource composition);

    void stop();

    void resume();

    void resume(int delay);

    void pause();

    void seekTo(long position);

    void setVolume(float volume);

    void setSoundBalance(SoundBalance soundBalance);

    Observable<Long> getTrackPositionObservable();

    void seekBy(long millis);

    Single<Long> getTrackPosition();

    void setPlaybackSpeed(float speed);

    Observable<Float> getCurrentPlaybackSpeedObservable();

    Observable<Boolean> getSpeedChangeAvailableObservable();
}
