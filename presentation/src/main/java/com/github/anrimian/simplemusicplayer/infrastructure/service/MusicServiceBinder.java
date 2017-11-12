package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.os.Binder;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created on 05.11.2017.
 */
public class MusicServiceBinder extends Binder {

    private MusicService musicService;
    private MediaSessionCompat mediaSession;

    MusicServiceBinder(MusicService musicService, MediaSessionCompat mediaSession) {
        this.musicService = musicService;
        this.mediaSession = mediaSession;
    }

    public MediaSessionCompat.Token getMediaSessionToken() {
        return mediaSession.getSessionToken();
    }

}
