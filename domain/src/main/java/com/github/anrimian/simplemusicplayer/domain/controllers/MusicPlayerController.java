package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import io.reactivex.Completable;

/**
 * Created on 10.11.2017.
 */

public interface MusicPlayerController {

    Completable play(Composition composition);

    void stop();

    void resume();
}
