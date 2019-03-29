package com.github.anrimian.musicplayer.ui.notifications.builder;

import android.content.Context;

import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import javax.annotation.Nullable;

import androidx.core.app.NotificationCompat.Builder;

import static com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer.FOREGROUND_CHANNEL_ID;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class AppNotificationBuilder {

    public Builder buildMusicNotification(Context context,
                                          @Nullable PlayQueueItem queueItem) {
        Builder builder = new Builder(context, FOREGROUND_CHANNEL_ID);

        builder.setColor(getColorFromAttr(context, android.R.attr.textColorPrimary));

        return builder;
    }
}
