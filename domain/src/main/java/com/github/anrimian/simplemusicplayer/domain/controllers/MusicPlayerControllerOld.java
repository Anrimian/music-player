package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

/**
 * Created on 03.11.2017.
 */

public interface MusicPlayerControllerOld {

    void play(List<Composition> compositions);

    void changePlayState();

    void skipToPrevious();

    void skipToNext();
}
