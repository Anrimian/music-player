package com.github.anrimian.musicplayer.data.controllers.music;

import android.content.Context;
import android.os.Handler;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParser;
import com.github.anrimian.musicplayer.data.controllers.music.players.AndroidMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.AppMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.CompositeMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.ExoMediaPlayer;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import java.util.ArrayList;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private final AppMediaPlayer mediaPlayer;
    private final UiStateRepository uiStateRepository;

    @Nullable
    private CompositionSource currentSource;
    private final BehaviorSubject<Float> currentSpeedSubject = BehaviorSubject.createDefault(1f);

    public MusicPlayerControllerImpl(UiStateRepository uiStateRepository,
                                     SettingsRepository settingsRepository,
                                     Context context,
                                     CompositionSourceProvider sourceRepository,
                                     Scheduler uiScheduler,
                                     Scheduler ioScheduler,
                                     PlayerErrorParser playerErrorParser,
                                     Analytics analytics,
                                     EqualizerController equalizerController) {
        this.uiStateRepository = uiStateRepository;

        int[] mediaPlayers = settingsRepository.getEnabledMediaPlayers();
        ArrayList<Function<AppMediaPlayer>> mediaPlayerImpls = new ArrayList<>(mediaPlayers.length);
        for (int playerId : mediaPlayers) {
            switch (playerId) {
                case MediaPlayers.EXO_MEDIA_PLAYER: {
                    mediaPlayerImpls.add(() -> new ExoMediaPlayer(context, sourceRepository, uiScheduler, ioScheduler, playerErrorParser, equalizerController));
                    break;
                }
                case MediaPlayers.ANDROID_MEDIA_PLAYER: {
                    mediaPlayerImpls.add(() -> new AndroidMediaPlayer(context, uiScheduler, sourceRepository, playerErrorParser, analytics, equalizerController));
                    break;
                }
            }

        }
        mediaPlayer = new CompositeMediaPlayer(mediaPlayerImpls);

//        mediaPlayer = new AndroidMediaPlayer(context, uiScheduler, sourceRepository, playerErrorParser, analytics, equalizerController);
//        mediaPlayer = new ExoMediaPlayer(context, sourceRepository, uiScheduler, playerErrorParser, equalizerController);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return mediaPlayer.getEventsObservable();
    }

    @Override
    public void prepareToPlay(CompositionSource source) {
        long trackPosition = getStartTrackPosition(source);
        currentSource = source;
        mediaPlayer.prepareToPlay(source, trackPosition, null);
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
        saveTrackPosition(0);
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        //noinspection ResultOfMethodCallIgnored
        mediaPlayer.getTrackPosition().subscribe(this::saveTrackPosition);
    }

    @Override
    public void seekTo(long position) {
        mediaPlayer.seekTo(position);
        saveTrackPosition(position);
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
    public void resume(int delay) {
        if (delay == 0) {
            resume();
        } else {
            new Handler().postDelayed(this::resume, delay);
        }
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return mediaPlayer.getTrackPositionObservable();
    }

    @Override
    public void seekBy(long millis) {
        //noinspection ResultOfMethodCallIgnored
        mediaPlayer.seekBy(millis).subscribe(this::saveTrackPosition);
    }

    @Override
    public Single<Long> getTrackPosition() {
        return mediaPlayer.getTrackPosition();
    }

    @Override
    public void setPlaybackSpeed(float speed) {
        mediaPlayer.setPlaySpeed(speed);
        currentSpeedSubject.onNext(speed);
    }

    @Override
    public float getCurrentPlaybackSpeed() {
        return currentSpeedSubject.getValue();
    }

    @Override
    public Observable<Float> getCurrentPlaybackSpeedObservable() {
        return currentSpeedSubject;
    }

    @Override
    public Observable<Boolean> getSpeedChangeAvailableObservable() {
        return mediaPlayer.getSpeedChangeAvailableObservable();
    }

    private void saveTrackPosition(long position) {
        if (currentSource instanceof LibraryCompositionSource) {
            uiStateRepository.setTrackPosition(position);
        }
    }

    private long getStartTrackPosition(CompositionSource source) {
        if (source instanceof LibraryCompositionSource) {
            return ((LibraryCompositionSource) source).getTrackPosition();
        }
        return 0;
    }
}
