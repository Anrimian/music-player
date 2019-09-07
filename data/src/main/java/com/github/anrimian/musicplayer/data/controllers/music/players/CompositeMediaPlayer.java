package com.github.anrimian.musicplayer.data.controllers.music.players;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class CompositeMediaPlayer implements MediaPlayer {

    private final MediaPlayer[] mediaPlayers;
    private final int startPlayerIndex;

    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    private MediaPlayer currentPlayer;
    private int currentPlayerIndex;

    public CompositeMediaPlayer(MediaPlayer[] mediaPlayers, int startPlayerIndex) {
        this.mediaPlayers = mediaPlayers;
        this.startPlayerIndex = startPlayerIndex;

        setPlayer(startPlayerIndex);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
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
        return trackPositionSubject;
    }

    @Override
    public long getTrackPosition() {
        return currentPlayer.getTrackPosition();
    }

    private void setPlayer(int index) {
        currentPlayerIndex = index;
        currentPlayer = mediaPlayers[index];

        playerDisposable.clear();
        playerDisposable.add(currentPlayer.getEventsObservable()
//                .doOnNext() // if error event, switch to another player and consume event
                .subscribe(playerEventSubject::onNext)
        );
        playerDisposable.add(currentPlayer.getTrackPositionObservable()
                .subscribe(trackPositionSubject::onNext)
        );
    }
}
