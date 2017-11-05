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

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.main.MainActivity;

/**
 * Created on 05.11.2017.
 */

public class NotificationsController {

    private static final String FOREGROUND_CHANNEL_ID = "0";

    public static final int FOREGROUND_NOTIFICATION_ID = 1;
    public static final String FOREGROUND_NOTIFICATION_DELETED = "foreground_notification_deleted";

    private NotificationManager notificationManager;

    private Context context;

    public NotificationsController(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    getString(R.string.foreground_channel_id),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false);
            channel.setVibrationPattern(new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0});//bug in system
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification getForegroundNotification() {
        return getDefaultMusicNotification().build();
    }

    public void displayStubForegroundNotification() {
        Intent intent = new Intent(FOREGROUND_NOTIFICATION_DELETED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        getDefaultMusicNotification()
                .setOngoing(false)
                .setDeleteIntent(pendingIntent);
    }

    private NotificationCompat.Builder getDefaultMusicNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContentTitle("test")
                .setSmallIcon(R.drawable.ic_menu)
                .setContentIntent(pIntent);
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
