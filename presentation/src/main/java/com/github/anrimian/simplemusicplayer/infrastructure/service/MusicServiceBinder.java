package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.os.Binder;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

/**
 * Created on 05.11.2017.
 */
public class MusicServiceBinder extends Binder {

    private MusicService musicService;

    MusicServiceBinder(MusicService musicService) {
        this.musicService = musicService;
    }

    public void play(List<Composition> compositions) {
        musicService.play(compositions);
    }

    public void pause() {
        musicService.pause();
    }

    public void resume() {
        musicService.resume();
    }
}
