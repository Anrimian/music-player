package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Notification;
import android.app.Service;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;

public interface MediaNotificationsDisplayer {

    void startStubForegroundNotification(Service service, MediaSessionCompat mediaSession);

    Notification getStubNotification(MediaSessionCompat mediaSession);

    void startForegroundNotification(Service service,
                                     int isPlayingState,
                                     @Nullable CompositionSource source,
                                     MediaSessionCompat mediaSession,
                                     int repeatMode,
                                     @Nullable MusicNotificationSetting notificationSetting,
                                     boolean reloadCover);

    void updateForegroundNotification(int isPlayingState,
                                      @Nullable CompositionSource source,
                                      MediaSessionCompat mediaSession,
                                      int repeatMode,
                                      MusicNotificationSetting notificationSetting,
                                      boolean reloadCover);

    void cancelCoverLoadingForForegroundNotification();

}
