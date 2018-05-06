package com.github.anrimian.simplemusicplayer.domain.controllers;

import com.github.anrimian.simplemusicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.Observable;

/**
 * Created on 10.12.2017.
 */

public interface SystemMusicController {

    @Nullable
    Observable<AudioFocusEvent> requestAudioFocus();

    Observable<Object> getAudioBecomingNoisyObservable();

}
