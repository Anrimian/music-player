package com.github.anrimian.musicplayer.infrastructure.service.music;

import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.Permissions;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtilsKt;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service {

    public static final String REQUEST_CODE = "request_code";
    public static final String START_FOREGROUND_SIGNAL = "start_foreground_signal";
    public static final String PLAY_DELAY_MILLIS = "play_delay";

    //optimization
    private final ServiceState serviceState = new ServiceState();

    private final CompositeDisposable serviceDisposable = new CompositeDisposable();

    private PlayerState playerState = PlayerState.IDLE.INSTANCE;
    private int isPlayingState;
    @Nullable
    private CompositionSource currentSource;
    private int repeatMode = RepeatMode.NONE;
    private MusicNotificationSetting notificationSetting;
    private AppTheme currentAppTheme;

    @Override
    public void onCreate() {
        super.onCreate();
        Components.getAppComponent().mediaSessionHandler().dispatchServiceCreated();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!Permissions.hasFilePermission(this)) {
            notificationsDisplayer().startForegroundErrorNotification(this, R.string.no_file_permission);
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent.getBooleanExtra(START_FOREGROUND_SIGNAL, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && playerState == PlayerState.IDLE.INSTANCE) {
                //should reduce chance of RemoteServiceException
                mediaNotificationsDisplayer().startStubForegroundNotification(this, mediaSession());
            }
            startForeground();
        }
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        if (requestCode != -1) {
            handleNotificationAction(requestCode, intent);
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Components.getAppComponent().mediaSessionHandler().dispatchServiceDestroyed();
        serviceDisposable.dispose();
    }

    public void startForeground() {
        //reduce chance to show first notification without info
        boolean reloadCover = false;
        if (notificationSetting == null) {
            reloadCover = true;
            currentSource = playerInteractor().getCurrentSource();
            notificationSetting = musicServiceInteractor().getNotificationSettings();
        }
        mediaNotificationsDisplayer().startForegroundNotification(this,
                isPlayingState,
                currentSource,
                mediaSession(),
                repeatMode,
                notificationSetting,
                reloadCover);

        subscribeOnServiceState();
    }

    private void handleNotificationAction(int requestCode, Intent intent) {
        switch (requestCode) {
            case PLAY: {
                int playDelay = intent.getIntExtra(PLAY_DELAY_MILLIS, 0);
                playerInteractor().play(playDelay);
                break;
            }
            case PAUSE: {
                playerInteractor().pause();
                break;
            }
            case SKIP_TO_NEXT: {
                musicServiceInteractor().skipToNext();
                break;
            }
            case SKIP_TO_PREVIOUS: {
                musicServiceInteractor().skipToPrevious();
                break;
            }
            case CHANGE_REPEAT_MODE: {
                musicServiceInteractor().changeRepeatMode();
                break;
            }
        }
    }

    private void subscribeOnServiceState() {
        if (serviceDisposable.size() != 0) {
            return;
        }
        serviceDisposable.add(Observable.combineLatest(playerInteractor().getIsPlayingStateObservable(),
                playerInteractor().getPlayerStateObservable(),
                playerInteractor().getCurrentSourceObservable(),
                musicServiceInteractor().getRepeatModeObservable(),
                musicServiceInteractor().getNotificationSettingObservable(),
                Components.getAppComponent().themeController().getAppThemeObservable(),
                serviceState::set)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onServiceStateReceived));

        serviceDisposable.add(Components.getAppComponent().systemServiceController()
                .getStopForegroundSignal()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> stopForeground(false)));

    }

    private void onServiceStateReceived(ServiceState serviceState) {
        CompositionSource newCompositionSource = serviceState.compositionSource.getValue();
        PlayerState newPlayerState = serviceState.playerState;

        boolean updateNotification = false;
        boolean updateCover = false;
        boolean stopService = false;

        if (this.playerState != serviceState.playerState) {
            this.playerState = serviceState.playerState;
        }

        int isPlayingState = FormatUtilsKt.getRemoteViewPlayerState(serviceState.isPlaying, serviceState.playerState);
        if (this.isPlayingState != isPlayingState) {
            this.isPlayingState = isPlayingState;
            updateNotification = true;
        }

        boolean isSourceEqual = Objects.equals(newCompositionSource, currentSource);
        boolean isContentEqual = CompositionSourceModelHelper.areSourcesTheSame(currentSource, newCompositionSource);
        if (!isSourceEqual || !isContentEqual) {
            this.currentSource = newCompositionSource;
            updateNotification = true;
            updateCover = true;
        }

        if (this.repeatMode != serviceState.repeatMode) {
            this.repeatMode = serviceState.repeatMode;
            updateNotification = true;
        }

        if (newCompositionSource == null || newPlayerState == PlayerState.IDLE.INSTANCE) {
            stopService = true;
        }

        MusicNotificationSetting newSettings = serviceState.settings;
        if (!newSettings.equals(this.notificationSetting)) {
            if (notificationSetting == null
                    || notificationSetting.isShowCovers() != newSettings.isShowCovers()
                    || notificationSetting.isColoredNotification() != newSettings.isColoredNotification()
                    || notificationSetting.isShowNotificationCoverStub() != newSettings.isShowNotificationCoverStub()) {
                updateNotification = true;
                updateCover = true;
            }
            this.notificationSetting = newSettings;
        }
        if (serviceState.appTheme != currentAppTheme) {
            currentAppTheme = serviceState.appTheme;
            updateNotification = true;
        }

        //seekbar values on cover settings change
        if (updateNotification && !stopService) {
            updateForegroundNotification(updateCover);
        }

        if (stopService) {
            mediaNotificationsDisplayer().cancelCoverLoadingForForegroundNotification();
            stopForeground(true);
            stopSelf();
        } else {
            if (!mediaSession().isActive()) {
                mediaSession().setActive(true);
            }
        }
    }

    private void updateForegroundNotification(boolean reloadCover) {
        mediaNotificationsDisplayer().updateForegroundNotification(
                isPlayingState,
                currentSource,
                mediaSession(),
                repeatMode,
                notificationSetting,
                reloadCover);
    }

    private MediaSessionCompat mediaSession() {
        return Components.getAppComponent().mediaSessionHandler().getMediaSession();
    }

    private PlayerInteractor playerInteractor() {
        return Components.getAppComponent().playerInteractor();
    }

    private MusicServiceInteractor musicServiceInteractor() {
        return Components.getAppComponent().musicServiceInteractor();
    }

    private MediaNotificationsDisplayer mediaNotificationsDisplayer() {
        return Components.getAppComponent().mediaNotificationsDisplayer();
    }

    private NotificationsDisplayer notificationsDisplayer() {
        return Components.getAppComponent().notificationsDisplayer();
    }

    private static class ServiceState {
        boolean isPlaying;
        PlayerState playerState;
        Optional<CompositionSource> compositionSource;
        int repeatMode;
        MusicNotificationSetting settings;
        AppTheme appTheme;

        private ServiceState set(boolean isPlaying,
                                 PlayerState playerState,
                                 Optional<CompositionSource> compositionSource,
                                 int repeatMode,
                                 MusicNotificationSetting settings,
                                 AppTheme appTheme) {
            this.isPlaying = isPlaying;
            this.playerState = playerState;
            this.compositionSource = compositionSource;
            this.repeatMode = repeatMode;
            this.settings = settings;
            this.appTheme = appTheme;
            return this;
        }
    }

    public class LocalBinder extends Binder {

        public MusicService getService() {
            return MusicService.this;
        }
    }

}
