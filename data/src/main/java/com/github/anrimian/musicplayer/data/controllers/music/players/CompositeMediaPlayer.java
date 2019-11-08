package com.github.anrimian.musicplayer.data.controllers.music.players;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class CompositeMediaPlayer implements MediaPlayer {

    private final MediaPlayer[] mediaPlayers;
    private final int startPlayerIndex = 0;

    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    private MediaPlayer currentPlayer;
    private int currentPlayerIndex;

    private Composition currentComposition;
    private long currentTrackPosition;

    public CompositeMediaPlayer(MediaPlayer[] mediaPlayers) {
        this.mediaPlayers = mediaPlayers;

        setPlayer(startPlayerIndex);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {
        currentComposition = composition;
        currentTrackPosition = startPosition;

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
                .flatMap(this::onPlayerEventReceived)
                .subscribe(playerEventSubject::onNext)
        );
        playerDisposable.add(currentPlayer.getTrackPositionObservable()
                .subscribe(this::onTrackPositionReceived)
        );
    }

    private void onTrackPositionReceived(long position) {
        this.currentTrackPosition = position;
        trackPositionSubject.onNext(position);
    }

    private Observable<PlayerEvent> onPlayerEventReceived(PlayerEvent event) {
        return Observable.create(emitter -> {
            // if error event, switch to another player and consume event
            if (event instanceof ErrorEvent) {
                if (((ErrorEvent) event).getErrorType() == ErrorType.UNKNOWN) {//unsupported instead?
                    int newPlayerIndex = currentPlayerIndex + 1;
                    //don't switch player when we reached end of available players
                    if (newPlayerIndex >= 0 && newPlayerIndex < mediaPlayers.length) {
                        setPlayer(newPlayerIndex);
                        currentPlayer.prepareToPlay(currentComposition, currentTrackPosition);
                        return;
                    }
                }
            }
            emitter.onNext(event);
        });
    }
}
