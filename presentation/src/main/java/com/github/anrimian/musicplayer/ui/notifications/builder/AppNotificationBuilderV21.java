package com.github.anrimian.musicplayer.ui.notifications.builder;

import android.content.Context;

import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import androidx.core.app.NotificationCompat.Builder;

import static com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer.FOREGROUND_CHANNEL_ID;

public class AppNotificationBuilderV21 extends AppNotificationBuilder {

    @Override
    public Builder buildMusicNotification(Context context, PlayQueueItem queueItem) {
        return new Builder(context, FOREGROUND_CHANNEL_ID);
    }
}
