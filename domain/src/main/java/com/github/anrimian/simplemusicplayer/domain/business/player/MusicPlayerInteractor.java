package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractor {

    private MusicPlayerController musicPlayerController;
    private MusicServiceController musicServiceController;
    private SettingsRepository settingsRepository;

    private PlayerState playerState = IDLE;
    private BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(playerState);
    private BehaviorSubject<Composition> currentCompositionSubject = BehaviorSubject.create();
    private BehaviorSubject<List<Composition>> currentPlayListSubject = BehaviorSubject.create();

    private List<Composition> currentPlayList = new ArrayList<>();
    private int currentPlayPosition;

    public MusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                 MusicServiceController musicServiceController,
                                 SettingsRepository settingsRepository) {
        this.musicPlayerController = musicPlayerController;
        this.musicServiceController = musicServiceController;
        this.settingsRepository = settingsRepository;
        subscribeOnInternalPlayerState();
    }

    public void startPlaying(List<Composition> compositions) {
        musicServiceController.start();
        currentPlayList.clear();
        currentPlayList.addAll(compositions);
        currentPlayListSubject.onNext(currentPlayList);
        currentPlayPosition = 0;
        playPosition();
    }

    public void play() {
        if (playerState != PLAY) {
            musicServiceController.start();
            musicPlayerController.resume();
            setState(PLAY);
        }
    }

    public void stop() {
        if (playerState != STOP) {
            musicPlayerController.stop();
            setState(STOP);
        }
    }

    public void skipToPrevious() {
        playPrevious();
    }

    public void skipToNext() {
        playNext(true);
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject;
    }

    public Observable<Composition> getCurrentCompositionObservable() {
        return currentCompositionSubject;
    }

    public Observable<List<Composition>> getCurrentPlayListObservable() {
        return currentPlayListSubject;
    }

    public boolean isInfinitePlayingEnabled() {
        return settingsRepository.isInfinitePlayingEnabled();
    }

    public boolean isRandomPlayingEnabled() {
        return settingsRepository.isRandomPlayingEnabled();
    }

    public void setRandomPlayingEnabled(boolean enabled) {
        settingsRepository.setRandomPlayingEnabled(enabled);
    }

    public void setInfinitePlayingEnabled(boolean enabled) {
        settingsRepository.setInfinitePlayingEnabled(enabled);
    }

    private void subscribeOnInternalPlayerState() {
        musicPlayerController.getPlayerStateObservable()
                .subscribe(this::onInternalPlayerStateChanged);
    }

    private void onInternalPlayerStateChanged(InternalPlayerState state) {
        switch (state) {
            case ENDED: {
                if (playerState == PLAY) {
                    playNext(false);
                }
            }
        }
    }

    private void setState(PlayerState playerState) {
//        if (this.playerState != playerState) {
            this.playerState = playerState;
            playerStateSubject.onNext(playerState);
//        }
    }

    private void playNext(boolean canScrollToFirst) {
        if (currentPlayPosition >= currentPlayList.size() - 1) {
            if (canScrollToFirst || settingsRepository.isInfinitePlayingEnabled()) {
                currentPlayPosition = 0;
            } else {
                stop();
                return;
            }
        } else {
            currentPlayPosition++;
        }
        playPosition();
    }

    private void playPrevious() {
        currentPlayPosition--;
        if (currentPlayPosition < 0) {
            currentPlayPosition = currentPlayList.size() - 1;
        }
        playPosition();
    }

    private void playPosition() {
        setState(LOADING);
        Composition composition = currentPlayList.get(currentPlayPosition);
        currentCompositionSubject.onNext(composition);
        musicPlayerController.play(composition)
                .subscribe(() -> {
                    setState(PLAY);
                }, throwable -> {
                    playNext(false);
                });
    }
}
