package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.business.player.state.PlayerStateInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import static com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController.FOREGROUND_NOTIFICATION_ID;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service/*MediaBrowserServiceCompat*/ {

    @Inject
    NotificationsController notificationsController;

    @Inject
    PlayerStateInteractor musicPlayerInteractor;

    private MusicServiceBinder musicServiceBinder = new MusicServiceBinder(this);

    private SimpleExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        Components.getAppComponent().inject(this);
//        RxReceivers.from(FOREGROUND_NOTIFICATION_DELETED, this)
//                .firstOrError()//TODO check for crashes, maybe we don't need it
//                .subscribe(o -> stopSelf());
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                Player.STA
                if (playWhenReady) {
                    musicPlayerInteractor.notifyResume();
                } else {
                    musicPlayerInteractor.notifyPause();
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }

/*    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }*/

    private void initializePlayer() {
//        Uri uri = Uri.parse(getString(R.string.media_url_mp3));
//        MediaSource mediaSource = buildMediaSource(uri);
//        player.prepare(mediaSource, true, false);



//        playerView.setPlayer(player);

//        player.setPlayWhenReady(true);
//        player.seekTo(currentWindow, playbackPosition);
    }

    public void play(List<Composition> compositions) {
        System.out.println(compositions);
        startForeground();
        Uri uri = Uri.fromFile(new File(compositions.get(compositions.size() - 1).getFilePath()));
        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
            musicPlayerInteractor.notifyPause();//TODO implement error behavior
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        player.prepare(audioSource);
        player.setPlayWhenReady(true);
    }

    public void pause() {
        System.out.println("pause");
        stopForeground();
        player.setPlayWhenReady(false);
    }

    public void resume() {
        System.out.println("resume");
        startForeground();
        player.setPlayWhenReady(true);
    }

    private void startForeground() {
        startForeground(FOREGROUND_NOTIFICATION_ID, notificationsController.getForegroundNotification());
    }

    private void stopForeground() {
        stopForeground(false);
//        notificationsController.displayStubForegroundNotification();
    }
}
