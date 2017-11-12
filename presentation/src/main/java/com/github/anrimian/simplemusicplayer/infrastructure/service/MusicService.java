package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.infrastructure.service.models.NotificationPlayerInfo;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsController.FOREGROUND_NOTIFICATION_ID;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service/*MediaBrowserServiceCompat*/ {

    public static final String REQUEST_CODE = "request_code";
    public static final int PLAY_PAUSE = 1;
    public static final int SKIP_TO_NEXT = 2;
    public static final int SKIP_TO_PREVIOUS = 3;

    @Inject
    NotificationsController notificationsController;

    @Inject
    MusicPlayerInteractor musicPlayerInteractor;

    private MusicServiceBinder musicServiceBinder = new MusicServiceBinder(this);

    private CompositeDisposable serviceDisposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        Components.getAppComponent().inject(this);
        subscribeOnPlayerActions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        switch (requestCode) {
            case PLAY_PAUSE: {
                musicPlayerInteractor.changePlayState();
                break;
            }
            case SKIP_TO_NEXT: {
                musicPlayerInteractor.skipToNext();
                break;
            }
            case SKIP_TO_PREVIOUS: {
                musicPlayerInteractor.skipToPrevious();
                break;
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceDisposable.dispose();
    }
/*    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }*/

    private void subscribeOnPlayerActions() {
        Observable.combineLatest(musicPlayerInteractor.getPlayerStateObservable(),
                musicPlayerInteractor.getCurrentCompositionObservable(),
                NotificationPlayerInfo::new)
                .subscribe(this::onNotificationInfoChanged);
    }

    private void onNotificationInfoChanged(NotificationPlayerInfo info) {
        notificationsController.updateForegroundNotification(info);
        switch (info.getState()) {
            case PLAYING: {
                startForeground(FOREGROUND_NOTIFICATION_ID, notificationsController.getForegroundNotification(info));
                break;
            }
            case STOP: {
                stopForeground(false);
                break;
            }
        }
    }
}
