package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder;

import static androidx.core.content.ContextCompat.getColor;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;
import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;


/**
 * Created on 05.11.2017.
 */

public class NotificationsDisplayer {

    public static final int FOREGROUND_NOTIFICATION_ID = 1;

    public static final String FOREGROUND_CHANNEL_ID = "0";

    private final Context context;
    private final NotificationManager notificationManager;
    private final AppNotificationBuilder notificationBuilder;

    public NotificationsDisplayer(Context context, AppNotificationBuilder notificationBuilder) {
        this.context = context;
        this.notificationBuilder = notificationBuilder;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    getString(R.string.foreground_channel_id),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification getForegroundNotification(boolean play,
                                                  @Nullable PlayQueueItem composition,
                                                  MediaSessionCompat mediaSession) {
        return getDefaultMusicNotification(play, composition, mediaSession).build();
    }

//    public Notification getStubNotification() {
//        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
//                .setContentTitle(getString(R.string.preparing_for_launch))
//                .setSmallIcon(R.drawable.ic_music_box)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .build();
//    }


    public void updateForegroundNotification(boolean play,
                                             @Nullable PlayQueueItem composition,
                                             MediaSessionCompat mediaSession) {
        Notification notification = getDefaultMusicNotification(play, composition, mediaSession).build();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }

    public void removePlayerNotification() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID);
    }

    private NotificationCompat.Builder getDefaultMusicNotification(boolean play,
                                                                   @Nullable PlayQueueItem queueItem,
                                                                   MediaSessionCompat mediaSession) {
        int requestCode = play? PAUSE : PLAY;
        Intent intentPlayPause = new Intent(context, MusicService.class);
        intentPlayPause.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pIntentPlayPause = PendingIntent.getService(context,
                requestCode,
                intentPlayPause,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentSkipToPrevious = new Intent(context, MusicService.class);
        intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
        PendingIntent pIntentSkipToPrevious = PendingIntent.getService(context,
                SKIP_TO_PREVIOUS,
                intentSkipToPrevious,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentSkipToNext = new Intent(context, MusicService.class);
        intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
        PendingIntent pIntentSkipToNext = PendingIntent.getService(context,
                SKIP_TO_NEXT,
                intentSkipToNext,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(OPEN_PLAY_QUEUE_ARG, true);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                play? R.drawable.ic_pause: R.drawable.ic_play,
                getString(play? R.string.pause: R.string.play),
                pIntentPlayPause);

        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
        style.setShowActionsInCompactView(0, 1, 2);

        NotificationCompat.Builder builder =  notificationBuilder.buildMusicNotification(context, queueItem)
//                .setColorized(true)
                .setColor(getColor(context, R.color.default_notification_color))
                .setSmallIcon(R.drawable.ic_music_box)
                .setContentIntent(pIntent)
                .addAction(R.drawable.ic_skip_previous, getString(R.string.previous_track), pIntentSkipToPrevious)
                .addAction(playPauseAction)
                .addAction(R.drawable.ic_skip_next, getString(R.string.next_track), pIntentSkipToNext)
                .setShowWhen(false)
                .setStyle(style)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (queueItem != null) {
            Composition composition = queueItem.getComposition();

//            Bitmap bitmap = getCompositionImage(composition);

//            int color;
//            if (bitmap == null) {
//                color = Color.WHITE;/*getColorFromAttr(context, android.R.attr.textColorPrimary);*/
//                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);//default icon
//            } else {
//                color = Palette.from(bitmap).generate().getDarkMutedColor(Color.WHITE);
//
//            }
//
            builder = builder.setContentTitle(formatCompositionName(composition))
                    .setContentText(formatCompositionAuthor(composition, context));
//                    .setColor(color)
//                    .setLargeIcon(bitmap);
        }
        return builder;
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
