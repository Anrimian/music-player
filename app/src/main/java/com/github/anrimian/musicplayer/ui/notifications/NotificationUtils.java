package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;
import android.os.DeadSystemException;
import android.service.notification.StatusBarNotification;

public class NotificationUtils {

    public static boolean isNotificationVisible(NotificationManager notificationManager,
                                                 int notificationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    if (notification.getId() == notificationId) {
                        return true;
                    }
                }
                return false;
            } catch (Exception ignored) {} //getActiveNotifications() can throw exception on android 6
        }
        return true;
    }


    public static void safeNotify(NotificationManager notificationManager,
                                   int id,
                                   Notification notification) {
        try {
            notificationManager.notify(id, notification);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && cause instanceof DeadSystemException) {
                return;
            }
            throw e;
        }
    }

}
