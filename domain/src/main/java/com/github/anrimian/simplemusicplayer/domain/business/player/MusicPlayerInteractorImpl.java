package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorImpl implements MusicPlayerInteractor {

    @Override
    public void startPlaying(Composition composition) {

    }

    @Override
    public void startPlaying(List<Composition> compositions) {

    }

    @Override
    public void stopPlaying() {

    }

    @Override
    public void resumePlaying() {

    }

    @Override
    public Observable<PlayerState> getPlayerStateObservable() {
        return null;
    }
}
