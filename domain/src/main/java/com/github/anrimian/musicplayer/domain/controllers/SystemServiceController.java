package com.github.anrimian.musicplayer.domain.controllers;

import io.reactivex.rxjava3.core.Observable;

public interface SystemServiceController {

    void startMusicService();

    default void stopMusicService() {
        stopMusicService(false);
    }

    void stopMusicService(boolean forceStop);

    Observable<Object> getStopForegroundSignal();

}
