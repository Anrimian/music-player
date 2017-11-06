package com.github.anrimian.simplemusicplayer.domain.business.player.state;

import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAYING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;

/**
 * Created on 06.11.2017.
 */

public class PlayerStateInteractorImpl implements PlayerStateInteractor {

    private BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(PlayerState.IDLE);

    @Override
    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject;
    }

    @Override
    public void notifyResume() {
        setState(PLAYING);
    }

    @Override
    public void notifyPause() {
        setState(STOP);
    }

    private void setState(PlayerState state) {
        playerStateSubject.onNext(state);
    }
}
