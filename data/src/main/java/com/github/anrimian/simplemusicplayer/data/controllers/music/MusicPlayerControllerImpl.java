package com.github.anrimian.simplemusicplayer.data.controllers.music;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.data.utils.exo_player.PlayerEventListener;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;
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
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();

    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private final PlayerEventListener playerEventListener = new PlayerEventListener(playerEventSubject);

    private final SimpleExoPlayer player;
    private final UiStatePreferences uiStatePreferences;

    @Nullable
    private Disposable trackPositionDisposable;

    public MusicPlayerControllerImpl(UiStatePreferences uiStatePreferences, Context context) {
        this.uiStatePreferences = uiStatePreferences;
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        player.addListener(playerEventListener);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventListener.getEventsObservable();
    }

    @Override
    public Completable prepareToPlay(CurrentComposition composition) {
        return prepareMediaSource(composition.getComposition())
                .toCompletable()//on error can be: com.google.android.exoplayer2.upstream.FileDataSource$FileDataSourceException: java.io.FileNotFoundException
                .doOnEvent(t -> onCompositionPrepared(t, composition));
    }

    @Override
    public void prepareToPlayIgnoreError(CurrentComposition composition) {
        prepareToPlay(composition)
                .onErrorComplete()
                .subscribe();
    }

    @Override
    public void stop() {
        seekTo(0);
        pause();
    }

    @Override
    public void pause() {
        player.setPlayWhenReady(false);
        stopTracingTrackPosition();
        uiStatePreferences.setTrackPosition(player.getCurrentPosition());
    }

    @Override
    public void seekTo(long position) {
        player.seekTo(position);
        trackPositionSubject.onNext(position);
        uiStatePreferences.setTrackPosition(player.getCurrentPosition());
    }

    @Override
    public void resume() {
        player.setPlayWhenReady(true);
        startTracingTrackPosition();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    private void onCompositionPrepared(Throwable throwable, CurrentComposition currentComposition) {
        if (throwable == null) {
            seekTo(currentComposition.getPlayPosition());
        } else {
            seekTo(0);
            playerEventSubject.onNext(new ErrorEvent(throwable));
        }
    }

    private void startTracingTrackPosition() {
        stopTracingTrackPosition();
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .map(o -> player.getCurrentPosition())
                .subscribe(trackPositionSubject::onNext);
    }

    private void stopTracingTrackPosition() {
        if (trackPositionDisposable != null) {
            trackPositionDisposable.dispose();
            trackPositionDisposable = null;
        }
    }

    private Single<MediaSource> prepareMediaSource(Composition composition) {
        return Single.create(emitter -> {
            Uri uri = Uri.fromFile(new File(composition.getFilePath()));
            DataSpec dataSpec = new DataSpec(uri);
            final FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
            } catch (FileDataSource.FileDataSourceException e) {
                emitter.onError(e);
            }

            DataSource.Factory factory = () -> fileDataSource;
            MediaSource mediaSource = new ExtractorMediaSource(fileDataSource.getUri(),
                    factory,
                    new DefaultExtractorsFactory(),
                    null,
                    null);
            player.prepare(mediaSource);
            emitter.onSuccess(mediaSource);
        });
    }

}
