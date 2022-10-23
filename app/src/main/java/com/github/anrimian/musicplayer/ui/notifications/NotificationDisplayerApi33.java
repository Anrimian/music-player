package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Service;

public class NotificationDisplayerApi33 implements NotificationsDisplayer {

    @Override
    public void showErrorNotification(int errorMessageId) {}

    @Override
    public void startForegroundErrorNotification(Service service, int errorMessageId) {}

    @Override
    public void removeErrorNotification() {}

}
