package com.github.anrimian.musicplayer.data.controllers.music;

import android.content.Context;

import com.github.anrimian.musicplayer.data.controllers.music.players.AndroidMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.MediaPlayer;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private final MediaPlayer mediaPlayer;
    private final UiStatePreferences uiStatePreferences;

    public MusicPlayerControllerImpl(UiStatePreferences uiStatePreferences,
                                     Context context,
                                     Scheduler scheduler,
                                     PlayerErrorParser playerErrorParser,
                                     Analytics analytics) {
        this.uiStatePreferences = uiStatePreferences;
//        Function<ExoMediaPlayer> exoMediaPlayer = () -> new ExoMediaPlayer(context, scheduler, playerErrorParser);
//        Function<MediaPlayer> androidMediaPlayer = () -> new AndroidMediaPlayer(scheduler, playerErrorParser);
//        mediaPlayer = new CompositeMediaPlayer(androidMediaPlayer);

//        mediaPlayer = new ExoMediaPlayer(context, scheduler, playerErrorParser);
        mediaPlayer = new AndroidMediaPlayer(scheduler, playerErrorParser, analytics);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return mediaPlayer.getEventsObservable();
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {
        mediaPlayer.prepareToPlay(composition, startPosition);
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
        uiStatePreferences.setTrackPosition(0);
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        uiStatePreferences.setTrackPosition(mediaPlayer.getTrackPosition());
    }

    @Override
    public void seekTo(long position) {
        mediaPlayer.seekTo(position);
        uiStatePreferences.setTrackPosition(position);
    }

    @Override
    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume);
    }

    @Override
    public void resume() {
        mediaPlayer.resume();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return mediaPlayer.getTrackPositionObservable();
    }

    @Override
    public long getTrackPosition() {
        return mediaPlayer.getTrackPosition();
    }
}
