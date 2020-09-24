package com.github.anrimian.musicplayer.domain.controllers;

import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created on 10.12.2017.
 */

public interface SystemMusicController {

    @Nullable
    Observable<AudioFocusEvent> requestAudioFocus();

    Observable<Object> getAudioBecomingNoisyObservable();

    Observable<Integer> getVolumeObservable();

}
