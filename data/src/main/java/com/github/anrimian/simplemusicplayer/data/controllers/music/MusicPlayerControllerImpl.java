package com.github.anrimian.simplemusicplayer.data.controllers.music;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.simplemusicplayer.data.utils.exo_player.PlayerStateRxWrapper;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
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

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private PlayerStateRxWrapper playerStateRxWrapper = new PlayerStateRxWrapper();
    private BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();

    private SimpleExoPlayer player;

    public MusicPlayerControllerImpl(Context context) {
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        player.addListener(playerStateRxWrapper);
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .map(o -> player.getCurrentPosition())
                .filter(o -> player.getPlayWhenReady())
                .subscribe(trackPositionSubject::onNext);
    }

    @Override
    public Completable prepareToPlay(Composition composition) {
        return prepareMediaSource(composition)
                .toCompletable()//on error can be: com.google.android.exoplayer2.upstream.FileDataSource$FileDataSourceException: java.io.FileNotFoundException
                .doOnComplete(() -> trackPositionSubject.onNext(0L));
    }

    @Override
    public void stop() {
        player.setPlayWhenReady(false);
        seekTo(0);
    }

    @Override
    public void pause() {
        player.setPlayWhenReady(false);
    }

    @Override
    public void seekTo(long position) {
        player.seekTo(position);
        trackPositionSubject.onNext(position);
    }

    @Override
    public void resume() {
        player.setPlayWhenReady(true);
    }

    @Override
    public Observable<InternalPlayerState> getPlayerStateObservable() {
        return playerStateRxWrapper.getStateObservable();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
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
