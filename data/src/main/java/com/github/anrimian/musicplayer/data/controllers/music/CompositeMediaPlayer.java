package com.github.anrimian.musicplayer.data.controllers.music;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;

public class CompositeMediaPlayer implements MediaPlayer {

    private final MediaPlayer[] mediaPlayers;
    private final int startPlayerIndex;

    private MediaPlayer currentPlayer;
    private int currentPlayerIndex;

    public CompositeMediaPlayer(MediaPlayer[] mediaPlayers, int startPlayerIndex) {
        this.mediaPlayers = mediaPlayers;
        this.startPlayerIndex = startPlayerIndex;

        setPlayer(startPlayerIndex);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return null;
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {
        if (currentPlayerIndex != startPlayerIndex) {
            setPlayer(startPlayerIndex);
        }
        currentPlayer.prepareToPlay(composition, startPosition);
    }

    @Override
    public void stop() {
        currentPlayer.stop();
    }

    @Override
    public void resume() {
        currentPlayer.resume();
    }

    @Override
    public void pause() {
        currentPlayer.pause();
    }

    @Override
    public void seekTo(long position) {
        currentPlayer.seekTo(position);
    }

    @Override
    public void setVolume(float volume) {
        currentPlayer.setVolume(volume);
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return null;
    }

    @Override
    public long getTrackPosition() {
        return currentPlayer.getTrackPosition();
    }

    private void setPlayer(int index) {
        currentPlayerIndex = index;
        currentPlayer = mediaPlayers[index];
    }
}
