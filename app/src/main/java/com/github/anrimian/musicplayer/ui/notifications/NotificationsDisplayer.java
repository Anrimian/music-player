package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Service;

import androidx.annotation.StringRes;

public interface NotificationsDisplayer {

    void showErrorNotification(@StringRes int errorMessageId);

    void startForegroundErrorNotification(Service service, @StringRes int errorMessageId);

    void removeErrorNotification();
}
