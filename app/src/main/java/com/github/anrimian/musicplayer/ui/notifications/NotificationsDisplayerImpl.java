package com.github.anrimian.musicplayer.ui.notifications;

import static androidx.core.content.ContextCompat.getColor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;

public class NotificationsDisplayerImpl implements NotificationsDisplayer {

    private static final int ERROR_NOTIFICATION_ID = 2;

    private static final String ERROR_CHANNEL_ID = "1";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationsDisplayerImpl(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel errorChannel = new NotificationChannel(ERROR_CHANNEL_ID,
                    context.getString(R.string.error_channel_description),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(errorChannel);
        }
    }

    @Override
    public void showErrorNotification(@StringRes int errorMessageId) {
        notificationManager.notify(ERROR_NOTIFICATION_ID, getErrorNotification(errorMessageId));
    }

    @Override
    public void startForegroundErrorNotification(Service service,
                                                 @StringRes int errorMessageId) {
        Notification notification = getErrorNotification(errorMessageId);
        service.startForeground(ERROR_NOTIFICATION_ID, notification);
    }

    @Override
    public void removeErrorNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID);
    }

    private Notification getErrorNotification(@StringRes int errorMessageId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,
                0,
                intent,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));

        return new NotificationCompat.Builder(context, ERROR_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.playing_error))
                .setContentText(context.getString(errorMessageId))
                .setColor(getColor(context, R.color.default_notification_color))
                .setSmallIcon(R.drawable.ic_music_box)
                .setVibrate(new long[]{100L, 100L})
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }

}
