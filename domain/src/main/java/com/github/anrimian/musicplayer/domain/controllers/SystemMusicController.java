package com.github.anrimian.musicplayer.domain.controllers;

import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState;

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

    Observable<VolumeState> getVolumeStateObservable();

    void setVolume(int volume);

    void changeVolumeBy(int volume);
}
