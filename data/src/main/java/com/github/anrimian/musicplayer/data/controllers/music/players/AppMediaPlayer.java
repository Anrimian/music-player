package com.github.anrimian.musicplayer.data.controllers.music.players;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface AppMediaPlayer {

    Observable<PlayerEvent> getEventsObservable();

    void prepareToPlay(CompositionSource composition,
                       long startPosition,
                       @Nullable ErrorType previousErrorType);

    void stop();

    void resume();

    void pause();

    void seekTo(long position);

    void setVolume(float volume);

    Observable<Long> getTrackPositionObservable();

    Single<Long> getTrackPosition();

    Single<Long> seekBy(long millis);

    void setPlaySpeed(float speed);

    float getPlaySpeed();

    void release();

    Observable<Boolean> getSpeedChangeAvailableObservable();

}
