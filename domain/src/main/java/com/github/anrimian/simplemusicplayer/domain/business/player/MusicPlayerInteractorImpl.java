package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAYING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorImpl implements MusicPlayerInteractor {

    private BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(PlayerState.IDLE);

    @Override
    public void startPlaying(Composition composition) {
        setState(PLAYING);
    }

    @Override
    public void startPlaying(List<Composition> compositions) {
        setState(PLAYING);
    }

    @Override
    public void stopPlaying() {
        setState(STOP);
    }

    @Override
    public void resumePlaying() {
        setState(PLAYING);
    }

    @Override
    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject;
    }

    private void setState(PlayerState state) {
        playerStateSubject.onNext(state);
    }
}
