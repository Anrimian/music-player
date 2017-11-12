package com.github.anrimian.simplemusicplayer.ui.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService;
import com.github.anrimian.simplemusicplayer.infrastructure.service.models.NotificationPlayerInfo;
import com.github.anrimian.simplemusicplayer.ui.main.MainActivity;

import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.PLAY_PAUSE;
import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.REQUEST_CODE;
import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.SKIP_TO_NEXT;
import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.SKIP_TO_PREVIOUS;


/**
 * Created on 05.11.2017.
 */

public class NotificationsController {

    private static final String FOREGROUND_CHANNEL_ID = "0";

    public static final int FOREGROUND_NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;

    private Context context;

    public NotificationsController(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    getString(R.string.foreground_channel_id),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification getForegroundNotification(NotificationPlayerInfo info) {
        return getDefaultMusicNotification(info).build();
    }

    public void updateForegroundNotification(NotificationPlayerInfo info) {
        Notification notification = getDefaultMusicNotification(info)
                .build();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private NotificationCompat.Builder getDefaultMusicNotification(NotificationPlayerInfo info) {
        boolean play = info.getState() == PlayerState.PLAYING;
        Composition composition = info.getComposition();

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_music);
        contentView.setImageViewResource(R.id.iv_play_stop, play? R.drawable.ic_pause : R.drawable.ic_play);

        contentView.setTextViewText(R.id.tv_description, composition.getDisplayName());

        Intent intentPlayPause = new Intent(context, MusicService.class);
        intentPlayPause.putExtra(REQUEST_CODE, PLAY_PAUSE);
        PendingIntent pIntentPlayPause = PendingIntent.getService(context, PLAY_PAUSE, intentPlayPause, PendingIntent.FLAG_CANCEL_CURRENT);
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

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContent(contentView)
                .setContentTitle("test")
                .setSmallIcon(R.drawable.ic_menu)
                .setContentIntent(pIntent);
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
