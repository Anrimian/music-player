package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAYING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorImpl implements MusicPlayerInteractor {

    private MusicPlayerController musicPlayerController;
    private MusicServiceController musicServiceController;

    private PlayerState playerState = IDLE;
    private BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(playerState);
    private BehaviorSubject<Composition> currentCompositionSubject = BehaviorSubject.create();

    private List<Composition> currentPlayList = new ArrayList<>();
    private int currentPlayPosition;

    private boolean repeat = false;//TODO move to preferences

    public MusicPlayerInteractorImpl(MusicPlayerController musicPlayerController,
                                     MusicServiceController musicServiceController) {
        this.musicPlayerController = musicPlayerController;
        this.musicServiceController = musicServiceController;
        subscribeOnInternalPlayerState();
    }

    @Override
    public void startPlaying(List<Composition> compositions) {
        musicServiceController.start();
        currentPlayList.clear();
        currentPlayList.addAll(compositions);
        currentPlayPosition = 0;
        playPosition();
    }

    @Override
    public void changePlayState() {
        if (playerState == PLAYING) {
            musicPlayerController.stop();
            setState(STOP);
        } else {
            musicServiceController.start();
            musicPlayerController.resume();
            setState(PLAYING);
        }
    }

    @Override
    public void skipToPrevious() {
        playPrevious();
    }

    @Override
    public void skipToNext() {
        playNext();
    }

    @Override
    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject;
    }

    @Override
    public Observable<Composition> getCurrentCompositionObservable() {
        return currentCompositionSubject;
    }

    private void subscribeOnInternalPlayerState() {
        musicPlayerController.getPlayerStateObservable()
                .subscribe(this::onInternalPlayerStateChanged);
    }

    private void onInternalPlayerStateChanged(InternalPlayerState state) {
        switch (state) {
            case ENDED: {
                if (playerState != STOP) {
                    playNext();
                }
            }
        }
    }

    private void setState(PlayerState playerState) {
        this.playerState = playerState;
        playerStateSubject.onNext(playerState);
    }

    private void playNext() {
        currentPlayPosition++;

        if (currentPlayPosition >= currentPlayList.size()) {
            if (repeat) {
                currentPlayPosition = 0;
            } else {
                musicPlayerController.stop();
                setState(STOP);
                return;
            }
        }

        playPosition();
    }

    private void playPrevious() {
        currentPlayPosition--;
        if (currentPlayPosition < 0) {
            currentPlayPosition = 0;
        }
        playPosition();
    }

    private void playPosition() {
        setState(LOADING);
        Composition composition = currentPlayList.get(currentPlayPosition);
        currentCompositionSubject.onNext(composition);
        musicPlayerController.play(composition)
                .subscribe(() -> {
                    setState(PLAYING);
                }, throwable -> {
                    playNext();
                });
    }
}
