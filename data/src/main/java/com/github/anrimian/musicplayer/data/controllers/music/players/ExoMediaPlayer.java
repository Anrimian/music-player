package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.musicplayer.data.utils.exo_player.PlayerEventListener;
import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class ExoMediaPlayer implements MediaPlayer {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();
    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    private final SimpleExoPlayer player;

    private final Scheduler scheduler;
    private final PlayerErrorParser playerErrorParser;

    @Nullable
    private Disposable trackPositionDisposable;

    private Composition currentComposition;

    public ExoMediaPlayer(Context context,
                          Scheduler scheduler,
                          PlayerErrorParser playerErrorParser) {
        this.scheduler = scheduler;
        player = ExoPlayerFactory.newSimpleInstance(
                context,
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        this.playerErrorParser = playerErrorParser;

        PlayerEventListener playerEventListener = new PlayerEventListener(
                () -> playerEventSubject.onNext(new FinishedEvent(currentComposition)),
                this::sendErrorEvent
        );
        player.addListener(playerEventListener);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {
        this.currentComposition = composition;
        checkComposition(composition)
                .flatMap(this::prepareMediaSource)
                .ignoreElement()
                .doOnEvent(t -> onCompositionPrepared(t, startPosition))
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void stop() {
        Completable.fromRunnable(() -> {
            seekTo(0);
            player.stop();
            stopTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void resume() {
        Completable.fromRunnable(() -> {
            player.setPlayWhenReady(true);
            startTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void pause() {
        Completable.fromRunnable(() -> {
            player.setPlayWhenReady(false);
            stopTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void seekTo(long position) {
        Completable.fromRunnable(() -> {
            player.seekTo(position);
            trackPositionSubject.onNext(position);
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void setVolume(float volume) {
        Completable.fromRunnable(() -> player.setVolume(volume))
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public long getTrackPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public void release() {
        player.release();
    }

    private void onCompositionPrepared(Throwable throwable, long startPosition) {
        if (throwable == null) {
            seekTo(startPosition);
            playerEventSubject.onNext(new PreparedEvent(currentComposition));
        } else {
            seekTo(0);
            player.setPlayWhenReady(false);
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

    private void startTracingTrackPosition() {
        stopTracingTrackPosition();
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(scheduler)
                .map(o -> player.getCurrentPosition())
                .subscribe(trackPositionSubject::onNext);
    }

    private void stopTracingTrackPosition() {
        if (trackPositionDisposable != null) {
            trackPositionDisposable.dispose();
            trackPositionDisposable = null;
        }
    }

    private Single<Composition> checkComposition(Composition composition) {
        return Single.fromCallable(() -> {
            File file = new File(composition.getFilePath());
            if (!file.exists()) {
                throw new FileNotFoundException(composition.getFilePath() + " not found");
            }
            return composition;
        });
    }

    private Single<MediaSource> prepareMediaSource(Composition composition) {
        return Single.create(emitter -> {
            Uri uri = Uri.fromFile(new File(composition.getFilePath()));
            DataSpec dataSpec = new DataSpec(uri);
            final FileDataSource fileDataSource = new FileDataSource();
            fileDataSource.open(dataSpec);

            DataSource.Factory factory = () -> fileDataSource;
            MediaSource mediaSource = new ExtractorMediaSource.Factory(factory)
                    .setExtractorsFactory(new DefaultExtractorsFactory())
                    .createMediaSource(uri);
            player.prepare(mediaSource);
            emitter.onSuccess(mediaSource);
        });
    }
}
