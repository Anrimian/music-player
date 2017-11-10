package com.github.anrimian.simplemusicplayer.infrastructure;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.utils.exo_player.PlayerStateRxWrapper;
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

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private PlayerStateRxWrapper playerStateRxWrapper = new PlayerStateRxWrapper();

    private SimpleExoPlayer player;

    public MusicPlayerControllerImpl(Context context) {
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        player.addListener(playerStateRxWrapper);
    }

    @Override
    public Completable play(Composition composition) {
        return prepareMediaSource(composition)
                .doOnSuccess(this::playMediaSource)
                .toCompletable();
    }

    @Override
    public void stop() {
        player.setPlayWhenReady(false);
    }

    @Override
    public void resume() {
        player.setPlayWhenReady(true);
    }

    @Override
    public Observable<InternalPlayerState> getPlayerStateObservable() {
        return playerStateRxWrapper.getStateObservable();
    }

    private void playMediaSource(MediaSource mediaSource) {
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
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
            MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                    factory,
                    new DefaultExtractorsFactory(),
                    null,
                    null);
            emitter.onSuccess(audioSource);
        });
    }

}
