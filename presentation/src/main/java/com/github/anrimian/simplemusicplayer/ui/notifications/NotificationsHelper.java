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
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.RemoteViews;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService;
import com.github.anrimian.simplemusicplayer.infrastructure.service.models.NotificationPlayerInfo;
import com.github.anrimian.simplemusicplayer.ui.main.MainActivity;

import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.PAUSE;
import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.PLAY;
import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.REQUEST_CODE;
import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.SKIP_TO_NEXT;
import static com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService.SKIP_TO_PREVIOUS;


/**
 * Created on 05.11.2017.
 */

public class NotificationsHelper {

    private static final String FOREGROUND_CHANNEL_ID = "0";

    public static final int FOREGROUND_NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;

    private Context context;

    public NotificationsHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    getString(R.string.foreground_channel_id),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification getForegroundNotification(NotificationPlayerInfo info, MediaSessionCompat mediaSession) {
        return getDefaultMusicNotification(info, mediaSession).build();
    }

    public void updateForegroundNotification(NotificationPlayerInfo info, MediaSessionCompat mediaSession) {
        Notification notification = getDefaultMusicNotification(info, mediaSession).build();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private NotificationCompat.Builder getDefaultMusicNotification(NotificationPlayerInfo info,
                                                                   MediaSessionCompat mediaSession) {
        boolean play = info.getState() == PlayerState.PLAY;
        Composition composition = info.getComposition();

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
                .setContentTitle("test")
                .setSmallIcon(R.drawable.ic_menu)
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
