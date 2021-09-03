package com.github.anrimian.musicplayer.data.controllers.music.players;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class CompositeMediaPlayer implements AppMediaPlayer {

    private final Function<AppMediaPlayer>[] mediaPlayers;
    private final int startPlayerIndex = 0;

    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();
    private final BehaviorSubject<Boolean> speedChangeAvailableSubject = BehaviorSubject.create();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    private AppMediaPlayer currentPlayer;
    private int currentPlayerIndex;

    private long currentTrackPosition;

    private float currentPlaySpeed = 1f;

    @SafeVarargs
    public CompositeMediaPlayer(Function<AppMediaPlayer>... mediaPlayers) {
        this.mediaPlayers = mediaPlayers;

        setPlayer(startPlayerIndex);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(CompositionSource composition,
                              long startPosition,
                              @Nullable ErrorType previousErrorType) {
        currentTrackPosition = startPosition;

        if (currentPlayerIndex != startPlayerIndex) {
            setPlayer(startPlayerIndex);
        }
        currentPlayer.prepareToPlay(composition, startPosition, previousErrorType);
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
    public Single<Long> getTrackPosition() {
        return currentPlayer.getTrackPosition();
    }

    @Override
    public Single<Long> seekBy(long millis) {
        return currentPlayer.seekBy(millis);
    }

    @Override
    public void setPlaySpeed(float speed) {
        this.currentPlaySpeed = speed;
        currentPlayer.setPlaySpeed(speed);
    }

    @Override
    public void release() {
        currentPlayer.release();
    }

    @Override
    public Observable<Boolean> getSpeedChangeAvailableObservable() {
        return speedChangeAvailableSubject;
    }

    private void setPlayer(int index) {
        currentPlayerIndex = index;
        if (currentPlayer != null) {
            currentPlayer.release();
        }
        currentPlayer = mediaPlayers[index].call();
        currentPlayer.setPlaySpeed(currentPlaySpeed);

        playerDisposable.clear();
        playerDisposable.add(currentPlayer.getEventsObservable()
                .flatMap(this::onPlayerEventReceived)
                .subscribe(playerEventSubject::onNext)
        );
        playerDisposable.add(currentPlayer.getTrackPositionObservable()
                .subscribe(this::onTrackPositionReceived)
        );
        playerDisposable.add(currentPlayer.getSpeedChangeAvailableObservable()
                .subscribe(speedChangeAvailableSubject::onNext));
    }

    private void onTrackPositionReceived(long position) {
        this.currentTrackPosition = position;
        trackPositionSubject.onNext(position);
    }

    private Observable<PlayerEvent> onPlayerEventReceived(PlayerEvent event) {
        return Observable.create(emitter -> {
            // if error event, switch to another player and consume event
            if (event instanceof ErrorEvent) {
                ErrorEvent errorEvent = ((ErrorEvent) event);
                ErrorType errorType = errorEvent.getErrorType();
                if (errorType == ErrorType.UNSUPPORTED) {
                    int newPlayerIndex = currentPlayerIndex + 1;
                    //don't switch player when we reached end of available players
                    if (newPlayerIndex >= 0 && newPlayerIndex < mediaPlayers.length) {
                        setPlayer(newPlayerIndex);
                        currentPlayer.prepareToPlay(errorEvent.getComposition(), currentTrackPosition, errorType);
                        return;
                    }
                }
            }
            emitter.onNext(event);
        });
    }
}
