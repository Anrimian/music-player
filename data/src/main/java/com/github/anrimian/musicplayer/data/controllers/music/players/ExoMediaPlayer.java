package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParser;
import com.github.anrimian.musicplayer.data.controllers.music.players.exoplayer.StereoVolumeProcessor;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.data.utils.exo_player.PlayerEventListener;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
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
    private final Scheduler uiScheduler;
    private final Scheduler ioScheduler;
    private final PlayerErrorParser playerErrorParser;
    private final EqualizerController equalizerController;

    private final StereoVolumeProcessor stereoVolumeProcessor = new StereoVolumeProcessor();

    private volatile ExoPlayer player;

    @Nullable
    private Disposable trackPositionDisposable;

    private CompositionSource currentComposition;

    private boolean isPreparing = false;
    private boolean playAfterPrepare = false;

    public ExoMediaPlayer(Context context,
                          CompositionSourceProvider sourceRepository,
                          Scheduler uiScheduler,
                          Scheduler ioScheduler,
                          PlayerErrorParser playerErrorParser,
                          EqualizerController equalizerController) {
        this.context = context;
        this.playerErrorParser = playerErrorParser;
        this.sourceRepository = sourceRepository;
        this.uiScheduler = uiScheduler;
        this.ioScheduler = ioScheduler;
        this.equalizerController = equalizerController;

        stereoVolumeProcessor.setChannelMap(new int[] { 0, 1 } );
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
        trackPositionSubject.onNext(startPosition);
        //cancel previous preparation?
        Single.fromCallable(() -> composition)
                .flatMapCompletable(this::prepareMediaSource)
                .doOnEvent(t -> onCompositionPrepared(t, startPosition))
                .onErrorComplete()
                .subscribeOn(uiScheduler)
                .subscribe();
    }

    @Override
    public void stop() {
        Completable.fromRunnable(() -> {
            seekTo(0);
            pausePlayer();
            stopTracingTrackPosition();
        }).subscribeOn(uiScheduler).subscribe();
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
        }).subscribeOn(uiScheduler).subscribe();
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
        }).subscribeOn(uiScheduler).subscribe();
    }

    @Override
    public void setVolume(float volume) {
        Completable.fromRunnable(() -> getPlayer().setVolume(volume))
                .subscribeOn(uiScheduler)
                .subscribe();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public Single<Long> getTrackPosition() {
        return Single.fromCallable(() -> getPlayer().getCurrentPosition())
                .subscribeOn(uiScheduler);
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
//            equalizerController.detachEqualizer();
            pausePlayer();
            stopTracingTrackPosition();
            player.release();
        });
    }

    @Override
    public Observable<Boolean> getSpeedChangeAvailableObservable() {
        return Observable.fromCallable(() -> true);
    }

    @Override
    public void setSoundBalance(SoundBalance soundBalance) {
        stereoVolumeProcessor.setVolume(soundBalance.getLeft(), soundBalance.getRight());
    }

    private void startPlayWhenReady() {
        Completable.fromRunnable(() -> {
            getPlayer().setPlayWhenReady(true);
            equalizerController.attachEqualizer(getPlayer().getAudioSessionId());
            startTracingTrackPosition();
        }).subscribeOn(uiScheduler).subscribe();
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
        equalizerController.detachEqualizer();
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
                .observeOn(uiScheduler)
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
                .subscribeOn(ioScheduler)
                .timeout(6, TimeUnit.SECONDS)//read from uri can be freeze for some reason, check
                .observeOn(uiScheduler)
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
        if (throwable instanceof PlaybackException) {
            Throwable cause = throwable.getCause();
            return cause instanceof Loader.UnexpectedLoaderException;
        }
        return false;
    }

    private Single<MediaSource> createMediaSource(Uri uri) {
        return Single.fromCallable(() -> {
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        });
    }

    private void usePlayer(Callback<ExoPlayer> function) {
        Completable.fromAction(() -> function.call(getPlayer()))
                .subscribeOn(uiScheduler)
                .subscribe();
    }

    private ExoPlayer getPlayer() {
        if (player == null) {
            synchronized (this) {
                if (player == null) {
                    RenderersFactory factory = createSimpleRenderersFactory(context, stereoVolumeProcessor);

                    player = new ExoPlayer.Builder(context, factory)
                            .build();

                    PlayerEventListener playerEventListener = new PlayerEventListener(
                            () -> playerEventSubject.onNext(new FinishedEvent(currentComposition)),
                            this::sendErrorEvent
                    );
                    player.addListener(playerEventListener);
//                    equalizerController.attachEqualizer(player.getAudioSessionId());
//                    player.addAnalyticsListener(new AnalyticsListener() {
//
//                        @Override
//                        public void onAudioSessionIdChanged(@NonNull EventTime eventTime, int audioSessionId) {
//                            equalizerController.attachEqualizer(audioSessionId);
//                        }

//                    });

                }
            }
        }
        return player;
    }

    private RenderersFactory createSimpleRenderersFactory(Context context,
                                                          AudioProcessor... audioProcessors) {
        return new DefaultRenderersFactory(context) {

            @Override
            protected AudioSink buildAudioSink(Context context1,
                                               boolean enableFloatOutput,
                                               boolean enableAudioTrackPlaybackParams,
                                               boolean enableOffload) {
                return new DefaultAudioSink(
                        AudioCapabilities.getCapabilities(context1),
                        new DefaultAudioSink.DefaultAudioProcessorChain(audioProcessors),
                        enableFloatOutput,
                        enableAudioTrackPlaybackParams,
                        enableOffload
                                ? DefaultAudioSink.OFFLOAD_MODE_ENABLED_GAPLESS_REQUIRED
                                : DefaultAudioSink.OFFLOAD_MODE_DISABLED);
            }
        };
    }

}
