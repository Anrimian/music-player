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

    public void changePlayState() {
        musicService.changePlayState();
    }

    public void skipToPrevious() {
        musicService.skipToPrevious();
    }

    public void skipToNext() {
        musicService.skipToNext();
    }
}
