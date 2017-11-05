package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.anrimian.simplemusicplayer.data.utils.rx_receivers.RxReceivers;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController;

import java.util.List;

import javax.inject.Inject;

import static com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController.FOREGROUND_NOTIFICATION_DELETED;
import static com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController.FOREGROUND_NOTIFICATION_ID;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service/*MediaBrowserServiceCompat*/ {

    @Inject
    NotificationsController notificationsController;

    private MusicServiceBinder musicServiceBinder = new MusicServiceBinder(this);

    @Override
    public void onCreate() {
        super.onCreate();
        Components.getAppComponent().inject(this);
        RxReceivers.from(FOREGROUND_NOTIFICATION_DELETED, this)
                .firstOrError()//TODO check for crashes, maybe we don't need it
                .subscribe(o -> stopSelf());
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
        /*Uri uri = Uri.parse(getString(R.string.media_url_mp3));
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

//        playerView.setPlayer(player);

        player.setPlayWhenReady(true);
//        player.seekTo(currentWindow, playbackPosition);*/
    }

    public void play(List<Composition> compositions) {
        System.out.println(compositions);
        startForeground();
    }

    public void pause() {
        System.out.println("pause");
        stopForeground();

    }

    public void resume() {
        System.out.println("resume");
        startForeground();
    }

    private void startForeground() {
        startForeground(FOREGROUND_NOTIFICATION_ID, notificationsController.getForegroundNotification());
    }

    private void stopForeground() {
        stopForeground(false);
        notificationsController.displayStubForegroundNotification();
    }
}
