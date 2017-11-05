package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service/*MediaBrowserServiceCompat*/ {

    public static final String CHANNEL_ID = "la-la-la";
    public static final String CHANNEL_NAME = "EXPRESS TAXI";

    private NotificationManager notificationManager;

    private MusicServiceBinder musicServiceBinder = new MusicServiceBinder(this);

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }

    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };




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
        startForeground(2, buildForegroundNotification(false));
        registerReceiver(receiver, new IntentFilter(NOTIFICATION_DELETED_ACTION));
    }

    private void stopForeground() {
        stopForeground(false);
        notificationManager.notify(2, buildForegroundNotification(true));
    }

    private Notification buildForegroundNotification(boolean stub) {
        Intent intent = new Intent(NOTIFICATION_DELETED_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("test, stub: " + stub)
                .setSmallIcon(R.drawable.ic_menu)
                .setOngoing(false)
                .setDeleteIntent(pendingIntent);
        return b.build();
    }
}
