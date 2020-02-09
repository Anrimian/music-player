package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils;
import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
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

import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;

public class AndroidMediaPlayer implements AppMediaPlayer {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();
    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    private final Scheduler scheduler;
    private final PlayerErrorParser playerErrorParser;
    private final Analytics analytics;

    private final MediaPlayer mediaPlayer;

    @Nullable
    private Disposable trackPositionDisposable;

    @Nullable
    private Disposable preparationDisposable;

    @Nullable
    private Composition currentComposition;

    private boolean isListenersInitialized = false;
    private boolean isSourcePrepared = false;
    private boolean playWhenReady = false;
    private boolean isPlaying = false;

    public AndroidMediaPlayer(Scheduler scheduler,
                              PlayerErrorParser playerErrorParser,
                              Analytics analytics) {
        this.scheduler = scheduler;
        this.playerErrorParser = playerErrorParser;
        this.analytics = analytics;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            Log.d("KEK2", "completed");
            if (currentComposition != null) {
                playerEventSubject.onNext(new FinishedEvent(currentComposition));
            }
        });
        mediaPlayer.setOnErrorListener((mediaPlayer, what, extra) -> {
            Log.d("KEK2", "error, what: " + what + ", extra: " + extra);
            sendErrorEvent(what, extra);
            return false;
        });
        //set new queue multiple times - play but not display play state(check, seems gone after fixes)
        //set new queue multiple times - run deleted item case
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject.doOnNext(event -> {
            Log.d("KEK1", "getEventsObservable, event: " + event);
        });
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {
        Log.d("KEK2", "prepare to play, startPosition: " + startPosition);
        this.currentComposition = composition;
        //check if file exists
        RxUtils.dispose(preparationDisposable);
        preparationDisposable = prepareMediaSource(composition)
                .doOnEvent(t -> onCompositionPrepared(t, startPosition))
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void stop() {
        if (!isPlaying) {
            return;
        }
        Log.d("KEK2", "stop");
        seekTo(0);
        stopTracingTrackPosition();
        mediaPlayer.stop();
        isPlaying = false;
        playWhenReady = false;
    }

    @Override
    public void resume() {
        if (isPlaying) {
            return;
        }
        Log.d("KEK2", "resume");
        if (isSourcePrepared) {
            start();
        }
        playWhenReady = true;
    }

    @Override
    public void pause() {
        if (!isPlaying) {
            return;
        }
        Log.d("KEK2", "pause");
        mediaPlayer.pause();
        stopTracingTrackPosition();
        isPlaying = false;
        playWhenReady = false;
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
        if (currentComposition == null) {
            return 0;
        }
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
            playerEventSubject.onNext(new PreparedEvent(currentComposition));
        } else {
            seekTo(0);
            mediaPlayer.pause();
            sendErrorEvent(throwable);
        }
    }

    private void sendErrorEvent(int what, int playerError) {
        if (currentComposition != null) {
            playerEventSubject.onNext(new ErrorEvent(
                    getErrorTypeFromPlayerError(what, playerError),
                    currentComposition)
            );
        }
    }

    private ErrorType getErrorTypeFromPlayerError(int what, int playerError) {
        switch (playerError) {
            case MEDIA_ERROR_UNSUPPORTED: {
                return ErrorType.UNSUPPORTED;
            }
            default: {
                analytics.logMessage("unknown player error, what: " + what + ", extra: " + playerError);
                return ErrorType.UNKNOWN;
            }
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
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(composition.getFilePath());
            mediaPlayer.prepare();
        }).doOnSubscribe(d -> isSourcePrepared = false)
                .doOnComplete(this::onSourcePrepared);
    }

    private void onSourcePrepared() {
        Log.d("KEK2", "onSourcePrepared, playWhenReady: " + playWhenReady);
        if (playWhenReady) {
            start();
        }
        isSourcePrepared = true;
    }

    private void start() {
        Log.d("KEK2", "start");
        mediaPlayer.start();

        //possible can solve smth
//        if (!isListenersInitialized) {
//            initializeListeners();
//            isListenersInitialized = true;
//        }

        startTracingTrackPosition();
        isPlaying = true;
    }

    private void initializeListeners() {
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            Log.d("KEK2", "completed");
            if (currentComposition != null) {
                playerEventSubject.onNext(new FinishedEvent(currentComposition));
            }
        });
        mediaPlayer.setOnErrorListener((mediaPlayer, what, extra) -> {
            Log.d("KEK2", "error, what: " + what + ", extra: " + extra);
            sendErrorEvent(what, extra);
            return false;
        });
    }
}
