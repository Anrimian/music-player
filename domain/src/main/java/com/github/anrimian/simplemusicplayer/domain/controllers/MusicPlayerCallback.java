package com.github.anrimian.simplemusicplayer.domain.controllers;

/**
 * Created on 17.04.2018.
 */
public interface MusicPlayerCallback {

    void onFinished();

    void onError(Throwable throwable);
}
