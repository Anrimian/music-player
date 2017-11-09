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
import com.github.anrimian.simplemusicplayer.utils.exo_player.ExoPlayerState;
import com.github.anrimian.simplemusicplayer.utils.exo_player.PlayerStateRxWrapper;
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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController.FOREGROUND_NOTIFICATION_ID;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service/*MediaBrowserServiceCompat*/ {

    public static final String REQUEST_CODE = "request_code";
    public static final int PLAY_PAUSE = 1;
    public static final int SKIP_TO_NEXT = 2;
    public static final int SKIP_TO_PREVIOUS = 3;

    @Inject
    NotificationsController notificationsController;

    @Inject
    PlayerStateInteractor musicPlayerInteractor;

    private MusicServiceBinder musicServiceBinder = new MusicServiceBinder(this);

    private SimpleExoPlayer player;

    private CompositeDisposable serviceDisposable = new CompositeDisposable();

    private PlayerStateRxWrapper playerStateRxWrapper = new PlayerStateRxWrapper();
    private Observable<ExoPlayerState> stateObservable = playerStateRxWrapper.getStateObservable();

    private List<Composition> currentPlayList = new ArrayList<>();
    private int currentPlayPosition;

    @Override
    public void onCreate() {
        super.onCreate();
        Components.getAppComponent().inject(this);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        player.addListener(playerStateRxWrapper);
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
                if (playWhenReady) {
                    if (playbackState == Player.STATE_ENDED) {
                        currentPlayPosition++;
                        playPosition();
                    }
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        switch (requestCode) {
            case PLAY_PAUSE: {
                changePlayState();
                break;
            }
            case SKIP_TO_NEXT: {
                skipToNext();
                break;
            }
            case SKIP_TO_PREVIOUS: {
                skipToPrevious();
                break;
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceDisposable.dispose();
    }
/*    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }*/

    public void play(List<Composition> compositions) {
        startForeground(FOREGROUND_NOTIFICATION_ID, notificationsController.getForegroundNotification(true));
        currentPlayList.clear();
        currentPlayList.addAll(compositions);
        currentPlayPosition = 0;
        playPosition();
        player.setPlayWhenReady(true);
    }

    private void playPosition() {
        if (currentPlayPosition >= currentPlayList.size() || currentPlayPosition < 0) {
            currentPlayPosition = 0;
        }
        Composition composition = currentPlayList.get(currentPlayPosition);

        prepareMediaSource(composition)
                .subscribe(mediaSource -> {
                    player.prepare(mediaSource);
                }, throwable -> {
                    currentPlayPosition++;//TODO case for only 1 element
                    playPosition();
                });
    }

    private Observable<MediaSource> prepareMediaSource(Composition composition) {
        return Observable.create(emitter -> {
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
            emitter.onNext(audioSource);
        });
    }

    public void changePlayState() {
        if (player.getPlayWhenReady()) {
            pause();
        } else {
            resume();
        }
    }

    public void skipToPrevious() {
        currentPlayPosition--;
        playPosition();
    }

    public void skipToNext() {
        currentPlayPosition++;
        playPosition();
    }

    private void pause() {
        stopForeground(false);
        notificationsController.updateForegroundNotification(false);
        player.setPlayWhenReady(false);
    }

    private void resume() {
        startForeground(FOREGROUND_NOTIFICATION_ID, notificationsController.getForegroundNotification(true));
        player.setPlayWhenReady(true);
    }
}
