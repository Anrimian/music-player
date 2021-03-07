package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParser;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.data.utils.exo_player.PlayerEventListener;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.Loader;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class ExoMediaPlayer implements AppMediaPlayer {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();
    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    private final Context context;
    private final CompositionSourceProvider sourceRepository;
    private final Scheduler scheduler;
    private final PlayerErrorParser playerErrorParser;
    private final EqualizerController equalizerController;

    private volatile SimpleExoPlayer player;

    @Nullable
    private Disposable trackPositionDisposable;

    private CompositionSource currentComposition;

    private boolean isPreparing = false;
    private boolean playAfterPrepare = false;

    public ExoMediaPlayer(Context context,
                          CompositionSourceProvider sourceRepository,
                          Scheduler scheduler,
                          PlayerErrorParser playerErrorParser,
                          EqualizerController equalizerController) {
        this.context = context;
        this.playerErrorParser = playerErrorParser;
        this.sourceRepository = sourceRepository;
        this.scheduler = scheduler;
        this.equalizerController = equalizerController;
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(CompositionSource composition,
                              long startPosition,
                              @Nullable ErrorType previousErrorType) {
        isPreparing = true;
        this.currentComposition = composition;
        Single.fromCallable(() -> composition)
                .flatMapCompletable(this::prepareMediaSource)
                .doOnEvent(t -> onCompositionPrepared(t, startPosition))
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void stop() {
        Completable.fromRunnable(() -> {
            seekTo(0);
            pausePlayer();
            stopTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void resume() {
        if (isPreparing) {
            playAfterPrepare = true;
            return;
        }
        startPlayWhenReady();
    }

    @Override
    public void pause() {
        Completable.fromRunnable(() -> {
            pausePlayer();
            stopTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void seekTo(long position) {
        Completable.fromRunnable(() -> {
            try {
                getPlayer().seekTo(position);
            } catch (IndexOutOfBoundsException ignored) {//crash inside exoplayer
                return;
            }
            trackPositionSubject.onNext(position);
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void setVolume(float volume) {
        Completable.fromRunnable(() -> getPlayer().setVolume(volume))
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public Single<Long> getTrackPosition() {
        return Single.fromCallable(() -> getPlayer().getCurrentPosition())
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Long> seekBy(long millis) {
        return getTrackPosition()
                .map(currentPosition -> {
                    long targetPosition = currentPosition + millis;
                    if (targetPosition < 0) {
                        targetPosition = 0;
                    }
                    if (targetPosition > getPlayer().getDuration()) {
                        return currentPosition;
                    }
                    seekTo(targetPosition);
                    return targetPosition;
                });
    }

    @Override
    public void setPlaySpeed(float speed) {
        usePlayer(player -> {
            PlaybackParameters param = new PlaybackParameters(speed);
            player.setPlaybackParameters(param);
        });
    }

    @Override
    public void release() {
        usePlayer(player -> {
            equalizerController.detachEqualizer();
            pausePlayer();
            stopTracingTrackPosition();
            player.release();
        });
    }

    @Override
    public Observable<Boolean> getSpeedChangeAvailableObservable() {
        return Observable.fromCallable(() -> true);
    }

    private void startPlayWhenReady() {
        Completable.fromRunnable(() -> {
            getPlayer().setPlayWhenReady(true);
            startTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    private void onCompositionPrepared(Throwable throwable, long startPosition) {
        isPreparing = false;
        if (throwable == null) {
            seekTo(startPosition);
            playerEventSubject.onNext(new PreparedEvent(currentComposition));
            if (playAfterPrepare) {
                playAfterPrepare = false;
                startPlayWhenReady();
            }
        } else {
            seekTo(0);
            pausePlayer();
            sendErrorEvent(throwable);
        }
    }

    private void pausePlayer() {
        getPlayer().setPlayWhenReady(false);
    }

    private void sendErrorEvent(Throwable throwable) {
        if (currentComposition != null) {
            //workaround for prepareError in newest exo player versions
            if (isStrangeLoaderException(throwable)) {
                prepareToPlay(currentComposition, getPlayer().getCurrentPosition(), null);
                return;
            }

            playerEventSubject.onNext(new ErrorEvent(
                    playerErrorParser.getErrorType(throwable),
                    currentComposition)
            );
        }
    }

    private void startTracingTrackPosition() {
        stopTracingTrackPosition();
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(scheduler)
                .map(o -> getPlayer().getCurrentPosition())
                .subscribe(trackPositionSubject::onNext);
    }

    private void stopTracingTrackPosition() {
        if (trackPositionDisposable != null) {
            trackPositionDisposable.dispose();
            trackPositionDisposable = null;
        }
    }

    private Completable prepareMediaSource(CompositionSource composition) {
        return getCompositionUri(composition)
                .flatMap(this::createMediaSource)
                .timeout(2, TimeUnit.SECONDS)//read from uri can be freeze for some reason, check
                .observeOn(scheduler)
                .doOnSuccess(mediaSource -> {
                    getPlayer().setMediaSource(mediaSource);
                    getPlayer().prepare();
                })
                .ignoreElement();
    }

    private Single<Uri> getCompositionUri(CompositionSource composition) {
        if (composition instanceof LibraryCompositionSource) {
            long id = ((LibraryCompositionSource) composition).getComposition().getId();
            return sourceRepository.getCompositionUri(id);
        }
        if (composition instanceof UriCompositionSource) {
            return Single.fromCallable(((UriCompositionSource) composition)::getUri);
        }
        throw new IllegalArgumentException("unknown composition source");
    }

    private boolean isStrangeLoaderException(Throwable throwable) {
        if (throwable instanceof ExoPlaybackException) {
            Throwable cause = throwable.getCause();
            return cause instanceof Loader.UnexpectedLoaderException;
        }
        return false;
    }

    private Single<MediaSource> createMediaSource(Uri uri) {
        return Single.fromCallable(() -> {
            DataSpec dataSpec = new DataSpec(uri);
            final ContentDataSource dataSource = new ContentDataSource(context);
            dataSource.open(dataSpec);

            DataSource.Factory factory = () -> dataSource;
            MediaItem mediaItem = new MediaItem.Builder().setUri(uri).build();
            return new ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem);
        });
    }

    private void usePlayer(Callback<SimpleExoPlayer> function) {
        Completable.fromAction(() -> function.call(getPlayer()))
                .subscribeOn(scheduler)
                .subscribe();
    }

    private SimpleExoPlayer getPlayer() {
        if (player == null) {
            synchronized (this) {
                if (player == null) {

                    player = new SimpleExoPlayer.Builder(context)
                            .build();

                    PlayerEventListener playerEventListener = new PlayerEventListener(
                            () -> playerEventSubject.onNext(new FinishedEvent(currentComposition)),
                            this::sendErrorEvent
                    );
                    player.addListener(playerEventListener);
                    equalizerController.attachEqualizer(player.getAudioSessionId());
                    player.addAnalyticsListener(new AnalyticsListener() {

                        @Override
                        public void onAudioSessionIdChanged(@NonNull EventTime eventTime, int audioSessionId) {
                            equalizerController.attachEqualizer(audioSessionId);
                        }

                    });

                }
            }
        }
        return player;
    }

}
