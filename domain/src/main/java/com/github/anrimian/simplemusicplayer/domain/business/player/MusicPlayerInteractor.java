package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.TrackState;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.CurrentPlayListInfo;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

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

    private static final int NO_POSITION = -1;

    private MusicPlayerController musicPlayerController;
    private MusicServiceController musicServiceController;
    private SettingsRepository settingsRepository;
    private UiStateRepository uiStateRepository;
    private PlayListRepository playListRepository;

    private PlayerState playerState = IDLE;
    private BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(playerState);
    private BehaviorSubject<Composition> currentCompositionSubject = BehaviorSubject.create();
    private BehaviorSubject<List<Composition>> currentPlayListSubject = BehaviorSubject.create();

    private List<Composition> initialPlayList;
    private List<Composition> currentPlayList = new ArrayList<>();

    private int currentPlayPosition = NO_POSITION;

    public MusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                 MusicServiceController musicServiceController,
                                 SettingsRepository settingsRepository,
                                 UiStateRepository uiStateRepository,
                                 PlayListRepository playListRepository) {
        this.musicPlayerController = musicPlayerController;
        this.musicServiceController = musicServiceController;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
        this.playListRepository = playListRepository;
        subscribeOnInternalPlayerState();
        restorePlaylistState();
    }

    public void startPlaying(List<Composition> compositions) {
        if (compositions == null || compositions.isEmpty()) {
            return;
        }
        initialPlayList = compositions;
        currentPlayList.clear();
        currentPlayList.addAll(compositions);
        shufflePlayList(false);
        saveCurrentPlayList();

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
                prepareToPlayPosition(true);
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
        shufflePlayList(true);
        saveCurrentPlayList();
        uiStateRepository.setPlayListPosition(currentPlayPosition);
    }

    public void setInfinitePlayingEnabled(boolean enabled) {
        settingsRepository.setInfinitePlayingEnabled(enabled);
    }

    public Observable<TrackState> getTrackStateObservable() {
        return musicPlayerController.getTrackStateObservable();
    }

    private void restorePlaylistState() {
        setState(LOADING);
        playListRepository.getCurrentPlayList()
                .subscribe(this::onPlayListRestored);
    }

    private void onPlayListRestored(CurrentPlayListInfo currentPlayListInfo) {
        List<Composition> initialPlayList = currentPlayListInfo.getInitialPlayList();
        List<Composition> currentPlayList = currentPlayListInfo.getCurrentPlayList();
        if (initialPlayList.isEmpty()) {
            setState(IDLE);
            return;
        }
        this.initialPlayList = initialPlayList;
        this.currentPlayList.clear();
        this.currentPlayList.addAll(currentPlayList);
        currentPlayListSubject.onNext(this.currentPlayList);
        currentPlayPosition = uiStateRepository.getPlayListPosition();
        prepareToPlayPosition(false);
    }

    private void subscribeOnInternalPlayerState() {
        musicPlayerController.getPlayerStateObservable()
                .subscribe(this::onInternalPlayerStateChanged);
    }

    private void shufflePlayList(boolean keepPosition) {
        Composition currentComposition = null;
        if (keepPosition) {
            currentComposition = currentPlayList.get(currentPlayPosition);
        }

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

    private void saveCurrentPlayList() {
        CurrentPlayListInfo currentPlayListInfo = new CurrentPlayListInfo(initialPlayList, currentPlayList);
        playListRepository.setCurrentPlayList(currentPlayListInfo)
                .subscribe();
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
        Composition currentComposition = currentPlayList.get(currentPlayPosition);
        currentCompositionSubject.onNext(currentComposition);
        uiStateRepository.setPlayListPosition(currentPlayPosition);
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
