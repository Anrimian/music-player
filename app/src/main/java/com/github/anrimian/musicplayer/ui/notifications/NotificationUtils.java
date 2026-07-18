package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import androidx.core.app.ServiceCompat;

import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

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
            if (AndroidUtils.isDeadSystemException(e)) {
                return;
            }
            throw e;
        }
    }

    public static void startMediaPlaybackForeground(Service service, int id, Notification notification) {
        int foregroundServiceType = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
        }
        ServiceCompat.startForeground(service, id, notification, foregroundServiceType);
    }

}
