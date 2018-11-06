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
import android.support.v4.media.session.MediaSessionCompat;

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
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.musicplayer.utils.AndroidUtils.getColorFromAttr;


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

    public Notification getForegroundNotification(@Nonnull PlayerMetaState state,
                                                  MediaSessionCompat mediaSession) {
        return getDefaultMusicNotification(state, mediaSession).build();
    }

//    public Notification getStubNotification() {
//        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
//                .setContentTitle(getString(R.string.preparing_for_launch))
//                .setSmallIcon(R.drawable.ic_music_box)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .build();
//    }


    public void updateForegroundNotification(@Nonnull PlayerMetaState state,
                                             MediaSessionCompat mediaSession) {
        Notification notification = getDefaultMusicNotification(state, mediaSession).build();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }

    public void removePlayerNotification() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID);
    }

    private NotificationCompat.Builder getDefaultMusicNotification(@Nonnull PlayerMetaState state,
                                                                   MediaSessionCompat mediaSession) {
        boolean play = state.getState() == PlayerState.PLAY;
        Composition composition = state.getQueueItem().getComposition();

        int requestCode = play? PAUSE : PLAY;
        Intent intentPlayPause = new Intent(context, MusicService.class);
        intentPlayPause.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pIntentPlayPause = PendingIntent.getService(context, requestCode, intentPlayPause, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentSkipToPrevious = new Intent(context, MusicService.class);
        intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
        PendingIntent pIntentSkipToPrevious = PendingIntent.getService(context, SKIP_TO_PREVIOUS, intentSkipToPrevious, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentSkipToNext = new Intent(context, MusicService.class);
        intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
        PendingIntent pIntentSkipToNext = PendingIntent.getService(context, SKIP_TO_NEXT, intentSkipToNext, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle style = new android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle();
        style.setShowActionsInCompactView(0, 1, 2);
//        style.setMediaSession(mediaSession.getSessionToken());

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                play? R.drawable.ic_pause: R.drawable.ic_play,
                getString(play? R.string.pause: R.string.play),
                pIntentPlayPause);

//        Bitmap bitmap = getCompositionImage(composition);
//        int color;
//        if (bitmap == null) {
//            color = Color.WHITE;/*getColorFromAttr(context, android.R.attr.textColorPrimary);*/
//            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_music_box);
//        } else {
//            color = Palette.from(bitmap).generate().getDarkMutedColor(Color.WHITE);
//        }

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setColor(getColorFromAttr(context, android.R.attr.textColorPrimary))
//                .setColorized(true)
                .setContentTitle(formatCompositionName(composition))
                .setContentText(composition.getArtist())
                .setSmallIcon(R.drawable.ic_music_box)
                .setContentIntent(pIntent)
                .addAction(R.drawable.ic_skip_previous, getString(R.string.previous_track), pIntentSkipToPrevious)
                .addAction(playPauseAction)
                .addAction(R.drawable.ic_skip_next, getString(R.string.next_track), pIntentSkipToNext)
                .setStyle(style)
                .setShowWhen(false)
//                .setLargeIcon(bitmap)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
