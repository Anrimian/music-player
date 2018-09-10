package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;
import com.github.anrimian.musicplayer.infrastructure.service.music.models.PlayerMetaState;
import com.github.anrimian.musicplayer.ui.main.MainActivity;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.PAUSE;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.PLAY;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.SKIP_TO_PREVIOUS;


/**
 * Created on 05.11.2017.
 */

public class NotificationsDisplayer {

    public static final int FOREGROUND_NOTIFICATION_ID = 1;

    private static final String FOREGROUND_CHANNEL_ID = "0";

    private NotificationManager notificationManager;
    private Context context;

    public NotificationsDisplayer(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    getString(R.string.foreground_channel_id),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification getForegroundNotification(@Nonnull PlayerMetaState state) {
        return getDefaultMusicNotification(state).build();
    }

    public Notification getStubNotification() {
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContentTitle(getString(R.string.preparing_for_launch))
                .setSmallIcon(R.drawable.ic_music_box)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }


    public void updateForegroundNotification(@Nonnull PlayerMetaState state) {
        Notification notification = getDefaultMusicNotification(state).build();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }

    public void removePlayerNotification() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID);
    }

    private NotificationCompat.Builder getDefaultMusicNotification(@Nonnull PlayerMetaState state) {
        boolean play = state.getState() == PlayerState.PLAY;
        Composition composition = state.getComposition();

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_music);
        contentView.setImageViewResource(R.id.iv_play_stop, play? R.drawable.ic_pause : R.drawable.ic_play);

        contentView.setTextViewText(R.id.tv_description, composition.getDisplayName());

        int requestCode = play? PAUSE : PLAY;
        Intent intentPlayPause = new Intent(context, MusicService.class);
        intentPlayPause.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pIntentPlayPause = PendingIntent.getService(context, requestCode, intentPlayPause, PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.iv_play_stop, pIntentPlayPause);

        Intent intentSkipToPrevious = new Intent(context, MusicService.class);
        intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
        PendingIntent pIntentSkipToPrevious = PendingIntent.getService(context, SKIP_TO_PREVIOUS, intentSkipToPrevious, PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.iv_skip_to_previous, pIntentSkipToPrevious);

        Intent intentSkipToNext = new Intent(context, MusicService.class);
        intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
        PendingIntent pIntentSkipToNext = PendingIntent.getService(context, SKIP_TO_NEXT, intentSkipToNext, PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.iv_skip_to_next, pIntentSkipToNext);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//        NotificationCompat.Style style = new android.support.v4.media.app.NotificationCompat.MediaStyle()
//                .setMediaSession(mediaSession.getSessionToken());

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContent(contentView)
//                .setContentTitle("test")
                .setSmallIcon(R.drawable.ic_music_box)
                .setContentIntent(pIntent)
//                .setStyle(style)
//                .setShowWhen(false)
//                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
