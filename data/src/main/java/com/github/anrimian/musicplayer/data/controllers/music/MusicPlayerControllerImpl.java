package com.github.anrimian.musicplayer.data.controllers.music;

import android.content.Context;

import com.github.anrimian.musicplayer.data.controllers.music.players.ExoMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.MediaPlayer;
import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private final MediaPlayer mediaPlayer;
    private final UiStateRepository uiStateRepository;

    public MusicPlayerControllerImpl(UiStateRepository uiStateRepository,
                                     Context context,
                                     Scheduler scheduler,
                                     PlayerErrorParser playerErrorParser) {
        this.uiStateRepository = uiStateRepository;
//        Function<ExoMediaPlayer> exoMediaPlayer = () -> new ExoMediaPlayer(context, scheduler, playerErrorParser);
//        Function<MediaPlayer> androidMediaPlayer = () -> new AndroidMediaPlayer(scheduler, playerErrorParser);
//        mediaPlayer = new CompositeMediaPlayer(androidMediaPlayer);

        mediaPlayer = new ExoMediaPlayer(context, scheduler, playerErrorParser);
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
        uiStateRepository.setTrackPosition(0);
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        uiStateRepository.setTrackPosition(mediaPlayer.getTrackPosition());
    }

    @Override
    public void seekTo(long position) {
        mediaPlayer.seekTo(position);
        uiStateRepository.setTrackPosition(position);
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
