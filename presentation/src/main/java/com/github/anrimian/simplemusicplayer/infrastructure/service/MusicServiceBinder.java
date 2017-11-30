package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.os.Binder;
import android.support.v4.media.session.MediaSessionCompat;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.List;

import io.reactivex.Observable;

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

    public void startPlaying(List<Composition> compositions) {
        musicService.startPlaying(compositions);
    }

    public void play() {
        musicService.play();
    }

    public void pause() {
        musicService.pause();
    }

    public void stop() {
        musicService.stop();
    }

    public void skipToPrevious() {
        musicService.skipToPrevious();
    }

    public void skipToNext() {
        musicService.skipToNext();
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return musicService.getPlayerStateObservable();
    }

    public Observable<Composition> getCurrentCompositionObservable() {
        return musicService.getCurrentCompositionObservable();
    }

    public Observable<List<Composition>> getCurrentPlayListObservable() {
        return musicService.getCurrentPlayListObservable();
    }

    public Observable<Long> getTrackPositionObservable() {
        return musicService.getTrackPositionObservable();
    }

    public boolean isInfinitePlayingEnabled() {
        return musicService.isInfinitePlayingEnabled();
    }

    public boolean isRandomPlayingEnabled() {
        return musicService.isRandomPlayingEnabled();
    }

    public void setRandomPlayingEnabled(boolean enabled) {
        musicService.setRandomPlayingEnabled(enabled);
    }

    public void setInfinitePlayingEnabled(boolean enabled) {
        musicService.setInfinitePlayingEnabled(enabled);
    }
}
