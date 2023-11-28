package com.github.anrimian.musicplayer.ui.notifications;

import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.Constants.Arguments.LAUNCH_PREPARE_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAYER_PANEL_ARG;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeIcon;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeText;
import static com.github.anrimian.musicplayer.ui.notifications.NotificationUtils.isNotificationVisible;
import static com.github.anrimian.musicplayer.ui.notifications.NotificationUtils.safeNotify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.github.anrimian.musicplayer.Constants;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtilsKt;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerActivity;
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;

import javax.annotation.Nonnull;

public class MediaNotificationsDisplayerApi33 implements MediaNotificationsDisplayer {

    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    public static final String FOREGROUND_CHANNEL_ID = "0";

    private final Context context;
    private final NotificationManager notificationManager;
    private final AppNotificationBuilder notificationBuilder;

    public MediaNotificationsDisplayerApi33(Context context,
                                            AppNotificationBuilder notificationBuilder) {
        this.context = context;
        this.notificationBuilder = notificationBuilder;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    context.getString(R.string.foreground_channel_description),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void startStubForegroundNotification(Service service, MediaSessionCompat mediaSession) {
        service.startForeground(FOREGROUND_NOTIFICATION_ID, getStubNotification(mediaSession));
    }

    @Override
    public Notification getStubNotification(MediaSessionCompat mediaSession) {
        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
        style.setMediaSession(mediaSession.getSessionToken());
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_music_box)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setStyle(style)
                .build();
    }

    @Override
    public void startForegroundNotification(Service service,
                                            int isPlayingState,
                                            @Nullable CompositionSource source,
                                            MediaSessionCompat mediaSession,
                                            int repeatMode,
                                            @Nullable MusicNotificationSetting notificationSetting,
                                            boolean reloadCover) {
        Notification notification = getDefaultMusicNotification(isPlayingState,
                source,
                mediaSession,
                repeatMode,
                notificationSetting)
                .build();
        service.startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    @Override
    public void updateForegroundNotification(int isPlayingState,
                                             @Nullable CompositionSource source,
                                             MediaSessionCompat mediaSession,
                                             int repeatMode,
                                             MusicNotificationSetting notificationSetting,
                                             boolean reloadCover) {
        if (!isNotificationVisible(notificationManager, FOREGROUND_NOTIFICATION_ID)) {
            return;
        }

        Notification notification = getDefaultMusicNotification(isPlayingState,
                source,
                mediaSession,
                repeatMode,
                notificationSetting)
                .build();
        safeNotify(notificationManager, FOREGROUND_NOTIFICATION_ID, notification);
    }

    @Override
    public void cancelCoverLoadingForForegroundNotification() {}

    private NotificationCompat.Builder getDefaultMusicNotification(int isPlayingState,
                                                                   @Nullable CompositionSource source,
                                                                   MediaSessionCompat mediaSession,
                                                                   int repeatMode,
                                                                   @Nullable MusicNotificationSetting notificationSetting) {
        Intent intent;
        if (source instanceof ExternalCompositionSource) {
            intent = new Intent(context, ExternalPlayerActivity.class);
            intent.putExtra(LAUNCH_PREPARE_ARG, false);
        } else {
            intent = new Intent(context, MainActivity.class);
            intent.putExtra(OPEN_PLAYER_PANEL_ARG, true);
        }
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));

        boolean coloredNotification = false;
        if (notificationSetting != null) {
            coloredNotification = notificationSetting.isColoredNotification();
        }

        NotificationCompat.Builder builder = notificationBuilder.buildMusicNotification(context)
                .setColorized(coloredNotification)
                .setSmallIcon(R.drawable.ic_music_box)
                .setContentIntent(pIntent)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_SERVICE);

        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
        style.setMediaSession(mediaSession.getSessionToken());
        builder.setStyle(style);

        if (source != null) {
            formatCompositionSource(source, builder);
            setActionsToNotification(isPlayingState, source, mediaSession, repeatMode, builder);
        }

        return builder;
    }

    private void setActionsToNotification(int isPlayingState,
                                          @Nonnull CompositionSource source,
                                          MediaSessionCompat mediaSession,
                                          int repeatMode,
                                          NotificationCompat.Builder builder) {
        int requestCode = isPlayingState == Constants.RemoteViewPlayerState.PAUSE? PLAY: PAUSE;
        Intent intentPlayPause = new Intent(context, MusicService.class);
        intentPlayPause.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pIntentPlayPause = PendingIntent.getService(context,
                requestCode,
                intentPlayPause,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                FormatUtilsKt.getRemoteViewPlayerStateIcon(isPlayingState),
                getString(isPlayingState == Constants.RemoteViewPlayerState.PAUSE? R.string.play: R.string.pause),
                pIntentPlayPause);

        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
        style.setMediaSession(mediaSession.getSessionToken());

        if (source instanceof LibraryCompositionSource) {
            Intent intentSkipToPrevious = new Intent(context, MusicService.class);
            intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
            PendingIntent pIntentSkipToPrevious = PendingIntent.getService(context,
                    SKIP_TO_PREVIOUS,
                    intentSkipToPrevious,
                    AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));

            Intent intentSkipToNext = new Intent(context, MusicService.class);
            intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
            PendingIntent pIntentSkipToNext = PendingIntent.getService(context,
                    SKIP_TO_NEXT,
                    intentSkipToNext,
                    AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));

            style.setShowActionsInCompactView(0, 1, 2);

            builder.addAction(R.drawable.ic_skip_previous, getString(R.string.previous_track), pIntentSkipToPrevious)
                    .addAction(playPauseAction)
                    .addAction(R.drawable.ic_skip_next, getString(R.string.next_track), pIntentSkipToNext);
        }
        if (source instanceof ExternalCompositionSource) {
            Intent intentChangeRepeatMode = new Intent(context, MusicService.class);
            intentChangeRepeatMode.putExtra(REQUEST_CODE, CHANGE_REPEAT_MODE);

            PendingIntent pIntentChangeRepeatMode = PendingIntent.getService(context,
                    CHANGE_REPEAT_MODE,
                    intentChangeRepeatMode,
                    AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));

            NotificationCompat.Action changeRepeatModeAction = new NotificationCompat.Action(
                    getRepeatModeIcon(repeatMode),
                    getString(getRepeatModeText(repeatMode)),
                    pIntentChangeRepeatMode);


            style.setShowActionsInCompactView(0, 1);

            builder.addAction(changeRepeatModeAction)
                    .addAction(playPauseAction);
        }

        builder.setStyle(style);
    }

    private void formatCompositionSource(@Nonnull CompositionSource source,
                                         NotificationCompat.Builder builder) {
        if (source instanceof LibraryCompositionSource) {
            Composition composition = ((LibraryCompositionSource) source).getComposition();
            builder.setContentTitle(formatCompositionName(composition))
                    .setContentText(formatCompositionAuthor(composition, context));
        }
        if (source instanceof ExternalCompositionSource) {
            ExternalCompositionSource eSource = (ExternalCompositionSource) source;
            builder.setContentTitle(formatCompositionName(eSource.getTitle(), eSource.getDisplayName()))
                    .setContentText(formatAuthor(eSource.getArtist(), context));
        }
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }

}
