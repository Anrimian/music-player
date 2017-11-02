package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created on 02.11.2017.
 */

public interface MusicPlayerInteractor {

    void startPlaying(Composition composition);

    void startPlaying(List<Composition> compositions);

    void stopPlaying();

    void resumePlaying();

    Observable<PlayerState> getPlayerStateObservable();

}
