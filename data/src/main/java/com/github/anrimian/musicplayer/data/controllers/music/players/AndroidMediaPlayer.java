package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.media.AudioManager;

import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class AndroidMediaPlayer implements MediaPlayer {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();
    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    private final Scheduler scheduler;
    private final PlayerErrorParser playerErrorParser;

    private android.media.MediaPlayer mediaPlayer;

    @Nullable
    private Disposable trackPositionDisposable;

    private Composition currentComposition;

    public AndroidMediaPlayer(Scheduler scheduler, PlayerErrorParser playerErrorParser) {
        this.scheduler = scheduler;
        this.playerErrorParser = playerErrorParser;
        mediaPlayer = new android.media.MediaPlayer();
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            sendErrorEvent(new Exception());
            mediaPlayer.reset();
            return true;
        });

    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {
        this.currentComposition = composition;
        //check if file exists
        prepareMediaSource(composition)
                .doOnEvent(t -> onCompositionPrepared(t, startPosition))
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void stop() {
        seekTo(0);
        stopTracingTrackPosition();
        mediaPlayer.stop();
//        mediaPlayer.reset();
    }

    @Override
    public void resume() {
        mediaPlayer.start();
        startTracingTrackPosition();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        stopTracingTrackPosition();
    }

    @Override
    public void seekTo(long position) {
        mediaPlayer.seekTo((int) position);
        trackPositionSubject.onNext(position);
    }

    @Override
    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume, volume);
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public long getTrackPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void release() {
        mediaPlayer.release();
    }

    private void startTracingTrackPosition() {
        stopTracingTrackPosition();
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(scheduler)
                .map(o -> (long) mediaPlayer.getCurrentPosition())
                .subscribe(trackPositionSubject::onNext);
    }

    private void stopTracingTrackPosition() {
        if (trackPositionDisposable != null) {
            trackPositionDisposable.dispose();
            trackPositionDisposable = null;
        }
    }

    private void onCompositionPrepared(Throwable throwable, long startPosition) {
        if (throwable == null) {
            seekTo(startPosition);
            playerEventSubject.onNext(new PreparedEvent());
        } else {
            seekTo(0);
            mediaPlayer.pause();
            sendErrorEvent(throwable);
        }
    }

    private void sendErrorEvent(Throwable throwable) {
        if (currentComposition != null) {
            playerEventSubject.onNext(new ErrorEvent(
                    playerErrorParser.getErrorType(throwable),
                    currentComposition)
            );
        }
    }

    private Completable prepareMediaSource(Composition composition) {
        return Completable.fromAction(() -> {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(composition.getFilePath());
            mediaPlayer.prepare();
        });
    }
}
