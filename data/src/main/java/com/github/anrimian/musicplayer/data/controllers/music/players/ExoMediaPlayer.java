package com.github.anrimian.musicplayer.data.controllers.music.players;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;

public class ExoMediaPlayer implements MediaPlayer {

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return null;
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return null;
    }

    @Override
    public long getTrackPosition() {
        return 0;
    }
}
