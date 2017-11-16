package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.TrackState;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
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
    private UiStateRepository uiStateRepository;

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
                                 SettingsRepository settingsRepository,
                                 UiStateRepository uiStateRepository) {
        this.musicPlayerController = musicPlayerController;
        this.musicServiceController = musicServiceController;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
        subscribeOnInternalPlayerState();
    }

    public void startPlaying(List<Composition> compositions) {
        if (compositions == null || compositions.isEmpty()) {
            return;
        }
        initialPlayList = compositions;
        currentComposition = null;
        shufflePlayList();
        currentPlayPosition = 0;
        prepareToPlayPosition(true);
    }

    public void play() {
        if (currentPlayList == null || currentPlayList.isEmpty()) {
            return;
        }

        switch (playerState) {
            case IDLE:
            case PLAY:
            case PAUSE: {
                musicServiceController.start();
                musicPlayerController.resume();
                setState(PLAY);
                return;
            }
            case STOP: {
                prepareToPlay(true);
            }
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
        moveToPrevious();
        prepareToPlayPosition(playerState == PLAY);
    }

    public void skipToNext() {
        moveToNext();
        prepareToPlayPosition(playerState == PLAY);
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
                    boolean playAfter = (currentPlayPosition < currentPlayList.size() - 2)
                            || settingsRepository.isInfinitePlayingEnabled();
                    moveToNext();
                    prepareToPlayPosition(playAfter);
                    break;
                }
            }
        }
    }

    private void setState(PlayerState playerState) {
        if (this.playerState != playerState) {
            this.playerState = playerState;
            playerStateSubject.onNext(playerState);
        }
    }

    private void moveToNext() {
        if (currentPlayPosition >= currentPlayList.size() - 1) {
            currentPlayPosition = 0;
        } else {
            currentPlayPosition++;
        }
    }

    private void moveToPrevious() {
        currentPlayPosition--;
        if (currentPlayPosition < 0) {
            currentPlayPosition = currentPlayList.size() - 1;
        }
    }

    private void prepareToPlayPosition(boolean playAfter) {
        moveToPosition();
        prepareToPlay(playAfter);
    }

    private void moveToPosition() {
        currentComposition = currentPlayList.get(currentPlayPosition);
        currentCompositionSubject.onNext(currentComposition);
    }

    private void prepareToPlay(boolean playAfter) {
        musicPlayerController.prepareToPlay(currentComposition)
                .subscribe(() -> {
                    if (playAfter) {
                        setState(PLAY);
                        play();
                    } else {
                        pause();
                    }
                }, throwable -> {
                    moveToNext();
                    prepareToPlayPosition(playAfter);
                });
    }
}
