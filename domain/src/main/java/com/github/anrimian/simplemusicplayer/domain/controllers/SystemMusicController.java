package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.player.AudioFocusEvent;

import io.reactivex.Observable;

/**
 * Created on 10.12.2017.
 */

public interface SystemMusicController {

    boolean requestAudioFocus();

    Observable<AudioFocusEvent> getAudioFocusObservable();

    void abandonAudioFocus();
}
