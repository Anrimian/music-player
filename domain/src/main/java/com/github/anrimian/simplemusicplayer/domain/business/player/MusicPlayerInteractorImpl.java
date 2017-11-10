package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerControllerOld;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAYING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorImpl implements MusicPlayerInteractor {

    private MusicPlayerControllerOld musicPlayerControllerOld;

    private MusicPlayerController musicPlayerController;
    private MusicServiceController musicServiceController;

    private BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(IDLE);

    private List<Composition> currentPlayList = new ArrayList<>();
    private int currentPlayPosition;

    private boolean repeat = false;//TODO move to preferences

    public MusicPlayerInteractorImpl(MusicPlayerControllerOld musicPlayerControllerOld) {
        this.musicPlayerControllerOld = musicPlayerControllerOld;
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
        if (playerStateSubject.getValue() == PLAYING) {
            musicServiceController.stop();
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
        currentPlayPosition--;
        playPosition();
//        musicPlayerControllerOld.skipToPrevious();
    }

    @Override
    public void skipToNext() {
        currentPlayPosition++;
        playPosition();
//        musicPlayerControllerOld.skipToNext();
    }

    @Override
    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject;
    }

    private void setState(PlayerState playerState) {
        playerStateSubject.onNext(playerState);
    }

    private void playPosition() {
        if (currentPlayPosition < 0) {
            currentPlayPosition = 0;
        }
        if (currentPlayPosition >= currentPlayList.size() && repeat) {
            currentPlayPosition = 0;
        } else {
            musicPlayerController.stop();
            setState(STOP);
        }
        Composition composition = currentPlayList.get(currentPlayPosition);

        musicPlayerController.play(composition)
                .subscribe(() -> {
                    currentPlayPosition++;
                    playPosition();
                }, throwable -> {
                    currentPlayPosition++;
                    playPosition();
                });
    }
}
