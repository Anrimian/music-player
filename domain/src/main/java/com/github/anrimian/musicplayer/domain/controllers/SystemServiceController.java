package com.github.anrimian.musicplayer.domain.controllers;

import io.reactivex.rxjava3.core.Observable;

public interface SystemServiceController {

    void startMusicService();

    void stopMusicService();

    Observable<Object> getStopForegroundSignal();

}
