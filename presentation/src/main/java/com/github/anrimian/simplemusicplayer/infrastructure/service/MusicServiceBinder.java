package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.os.Binder;

/**
 * Created on 05.11.2017.
 */
public class MusicServiceBinder extends Binder {

    private MusicService musicService;

    MusicServiceBinder(MusicService musicService) {
        this.musicService = musicService;
    }


}
