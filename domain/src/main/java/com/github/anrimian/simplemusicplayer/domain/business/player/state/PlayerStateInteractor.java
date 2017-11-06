package com.github.anrimian.simplemusicplayer.domain.business.player.state;

import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import io.reactivex.Observable;

/**
 * Created on 06.11.2017.
 */

public interface PlayerStateInteractor {

    Observable<PlayerState> getPlayerStateObservable();

    void notifyResume();

    void notifyPause();
}
