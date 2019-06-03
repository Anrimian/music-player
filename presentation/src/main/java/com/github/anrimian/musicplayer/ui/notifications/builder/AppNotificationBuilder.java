package com.github.anrimian.musicplayer.ui.notifications.builder;

import android.content.Context;

import androidx.core.app.NotificationCompat.Builder;

import static com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer.FOREGROUND_CHANNEL_ID;

public class AppNotificationBuilder {

    public Builder buildMusicNotification(Context context) {
        return new Builder(context, FOREGROUND_CHANNEL_ID);
    }
}
