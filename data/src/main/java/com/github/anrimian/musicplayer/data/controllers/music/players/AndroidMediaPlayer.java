package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParser;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.data.utils.rx.RxUtils;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

import static android.media.MediaPlayer.MEDIA_ERROR_MALFORMED;
import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;

public class AndroidMediaPlayer implements AppMediaPlayer {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();
    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    private final Context context;
    private final Scheduler scheduler;
    private final CompositionSourceProvider sourceRepository;
    private final PlayerErrorParser playerErrorParser;
    private final Analytics analytics;
    private final EqualizerController equalizerController;

    private final MediaPlayer mediaPlayer;

    @Nullable
    private Disposable trackPositionDisposable;

    @Nullable
    private Disposable preparationDisposable;

    @Nullable
    private CompositionSource currentComposition;

    private boolean isSourcePrepared = false;
    private boolean playWhenReady = false;
    private boolean isPlaying = false;

    @Nullable
    private ErrorType previousErrorType;

    //problem with error case(file not found), multiple error events
    public AndroidMediaPlayer(Context context,
                              Scheduler scheduler,
                              CompositionSourceProvider sourceRepository,
                              PlayerErrorParser playerErrorParser,
                              Analytics analytics,
                              EqualizerController equalizerController) {
        this.context = context;
        this.scheduler = scheduler;
        this.sourceRepository = sourceRepository;
        this.playerErrorParser = playerErrorParser;
        this.analytics = analytics;
        this.equalizerController = equalizerController;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (currentComposition != null) {
                playerEventSubject.onNext(new FinishedEvent(currentComposition));
            }
        });
        mediaPlayer.setOnErrorListener((mediaPlayer, what, extra) -> {
            sendErrorEvent(what, extra);
            return false;
        });

        try {
            equalizerController.attachEqualizer(mediaPlayer.getAudioSessionId());
        } catch (IllegalStateException ignored) {}
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(CompositionSource composition,
                              long startPosition,
                              @Nullable ErrorType previousErrorType) {
        this.currentComposition = composition;
        this.previousErrorType = previousErrorType;
        RxUtils.dispose(preparationDisposable);
        preparationDisposable = Single.fromCallable(() -> composition)
                .flatMapCompletable(this::prepareMediaSource)
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
        if (isSourcePrepared) {
            seekTo(0);
        }
        stopTracingTrackPosition();
        if (isSourcePrepared) {
            pausePlayer();
        }
        isPlaying = false;
        playWhenReady = false;
    }

    @Override
    public void resume() {
        if (isPlaying) {
            return;
        }
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
        stopTracingTrackPosition();
        pausePlayer();
        isPlaying = false;
        playWhenReady = false;
    }

    @Override
    public void seekTo(long position) {
        try {
            if (isSourcePrepared) {
                mediaPlayer.seekTo((int) position);
            }
        } catch (IllegalStateException ignored) {}
        trackPositionSubject.onNext(position);
    }

    @Override
    public void setVolume(float volume) {
        try {
            mediaPlayer.setVolume(volume, volume);
        } catch (IllegalStateException ignored) {}
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public Single<Long> getTrackPosition() {
        return Single.fromCallable(() -> {
            if (currentComposition == null) {
                return 0L;
            }
            try {
                return (long) mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                return 0L;
            }
        });
    }

    @Override
    public Single<Long> seekBy(long millis) {
        return getTrackPosition()
                .map(currentPosition -> {
                    try {
                        long targetPosition = currentPosition + millis;
                        if (targetPosition < 0) {
                            targetPosition = 0;
                        }
                        if (targetPosition > mediaPlayer.getDuration()) {
                            return currentPosition;
                        }
                        seekTo(targetPosition);
                        return targetPosition;

                    } catch (IllegalStateException ignored) {
                        return currentPosition;
                    }
                });
    }

    @Override
    public void release() {
        equalizerController.detachEqualizer();
        stopTracingTrackPosition();
        mediaPlayer.release();
    }

    private void startTracingTrackPosition() {
        stopTracingTrackPosition();
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(scheduler)
                .flatMapSingle(o -> getTrackPosition())
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
            pausePlayer();
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
            case MEDIA_ERROR_UNSUPPORTED:
            case MEDIA_ERROR_MALFORMED: {
                return ErrorType.UNSUPPORTED;
            }
            default: {
                analytics.logMessage("unknown player error, what: " + what + ", extra: " + playerError);
                return ErrorType.IGNORED;
            }
        }
    }

    private void sendErrorEvent(Throwable throwable) {
        if (currentComposition != null) {
            ErrorType errorType;
            if (throwable instanceof IOException && previousErrorType == ErrorType.UNSUPPORTED) {
                errorType = ErrorType.UNSUPPORTED;
                previousErrorType = null;
            } else {
                errorType = playerErrorParser.getErrorType(throwable);
            }
            playerEventSubject.onNext(new ErrorEvent(errorType, currentComposition));
        }
    }

    private Completable prepareMediaSource(CompositionSource composition) {
        return prepareMediaSourceInternal(composition)
                .doOnSubscribe(d -> isSourcePrepared = false)
                .doOnComplete(this::onSourcePrepared);
    }

    private Completable prepareMediaSourceInternal(CompositionSource composition) {
        if (composition instanceof LibraryCompositionSource) {
            long id = ((LibraryCompositionSource) composition).getComposition().getId();
            return sourceRepository.getCompositionFileDescriptorSingle(id)
                    .doOnSuccess(fileDescriptor -> {
                        mediaPlayer.reset();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(fileDescriptor);
                        mediaPlayer.prepare();
                    })
                    .ignoreElement();
        }
        if (composition instanceof UriCompositionSource) {
            return Single.fromCallable(((UriCompositionSource) composition)::getUri)
                    .doOnSuccess(uri -> {
                        mediaPlayer.reset();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(context, uri);
                        mediaPlayer.prepare();
                    })
                    .ignoreElement();

        }
        throw new IllegalArgumentException("unknown composition source");
    }

    private void onSourcePrepared() {
        if (playWhenReady) {
            start();
        }
        isSourcePrepared = true;
    }

    private void pausePlayer() {
        pause(mediaPlayer);
    }

    private void start() {
        start(mediaPlayer);
        startTracingTrackPosition();
        isPlaying = true;
    }

    private void pause(MediaPlayer mediaPlayer) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception ignored) {}
    }

    private void start(MediaPlayer mediaPlayer) {
        try {
            mediaPlayer.start();
        } catch (IllegalStateException ignored) {}
    }

}
