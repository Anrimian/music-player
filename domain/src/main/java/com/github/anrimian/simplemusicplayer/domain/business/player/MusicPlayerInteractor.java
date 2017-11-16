package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.TrackState;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PAUSE;
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

    private List<Composition> initialPlayList;
    private List<Composition> currentPlayList = new ArrayList<>();

    private int currentPlayPosition;
    private Composition currentComposition;

    public MusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                 MusicServiceController musicServiceController,
                                 SettingsRepository settingsRepository) {
        this.musicPlayerController = musicPlayerController;
        this.musicServiceController = musicServiceController;
        this.settingsRepository = settingsRepository;
        subscribeOnInternalPlayerState();
    }

    public void startPlaying(List<Composition> compositions) {
        if (compositions == null || compositions.isEmpty()) {
            return;
        }
        musicServiceController.start();
        initialPlayList = compositions;
        currentComposition = null;
        shufflePlayList();
        currentPlayPosition = 0;
        moveToPosition();
        playPosition();
    }

    public void play() {
        if (playerState == PLAY || playerState == IDLE) {
            return;
        }
        musicServiceController.start();
        if (playerState == PAUSE) {
            musicPlayerController.resume();
            setState(PLAY);
            return;
        }
        if (playerState == STOP) {
            if (currentPlayPosition < 0) {
                currentPlayPosition = 0;
            }
            if (currentPlayPosition >= currentPlayList.size()) {
                currentPlayPosition = currentPlayList.size() - 1;
            }
            moveToPosition();
            playPosition();
        }
    }

    public void pause() {
        if (playerState != PAUSE) {
            musicPlayerController.stop();
            setState(PAUSE);
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
        shufflePlayList();
    }

    public void setInfinitePlayingEnabled(boolean enabled) {
        settingsRepository.setInfinitePlayingEnabled(enabled);
    }

    public Observable<TrackState> getTrackStateObservable() {
        return musicPlayerController.getTrackStateObservable();
    }

    private void subscribeOnInternalPlayerState() {
        musicPlayerController.getPlayerStateObservable()
                .subscribe(this::onInternalPlayerStateChanged);
    }

    private void shufflePlayList() {
        currentPlayList.clear();
        if (settingsRepository.isRandomPlayingEnabled()) {
            List<Composition> playListToShuffle = new ArrayList<>(initialPlayList);
            if (currentComposition != null) {
                playListToShuffle.remove(currentPlayPosition);
                currentPlayList.add(currentComposition);
            }
            Collections.shuffle(playListToShuffle);
            currentPlayList.addAll(playListToShuffle);
        } else {
            currentPlayList.addAll(initialPlayList);
        }
        if (currentComposition != null) {
            currentPlayPosition = currentPlayList.indexOf(currentComposition);
        }
        currentPlayListSubject.onNext(currentPlayList);
    }

    private void onInternalPlayerStateChanged(InternalPlayerState state) {
        switch (state) {
            case ENDED: {
                if (playerState == PLAY) {
                    playNext(false);
                    break;
                }
            }
        }
    }

    private void setState(PlayerState playerState) {
        this.playerState = playerState;
        playerStateSubject.onNext(playerState);
    }

    private void playNext(boolean canMoveToStart) {
        if (currentPlayPosition >= currentPlayList.size() - 1) {
            if (canMoveToStart || settingsRepository.isInfinitePlayingEnabled()) {
                currentPlayPosition = 0;
            } else {
                stop();
                return;
            }
        } else {
            currentPlayPosition++;
        }
        moveToPosition();
        if (playerState == PLAY) {
            playPosition();
        } else {
            setState(STOP);
        }
    }

    private void playPrevious() {
        currentPlayPosition--;
        if (currentPlayPosition < 0) {
            currentPlayPosition = currentPlayList.size() - 1;
        }
        moveToPosition();
        if (playerState == PLAY) {
            playPosition();
        } else {
            setState(STOP);
        }
    }

    private void moveToPosition() {
        currentComposition = currentPlayList.get(currentPlayPosition);
        currentCompositionSubject.onNext(currentComposition);
    }

    private void playPosition() {
        setState(LOADING);
        musicPlayerController.play(currentComposition)
                .subscribe(() -> {
                    setState(PLAY);
                }, throwable -> {
                    playNext(false);
                });
    }
}
