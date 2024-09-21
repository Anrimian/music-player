package com.github.anrimian.musicplayer.infrastructure.service;

import android.app.ForegroundServiceStartNotAllowedException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.Constants;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.Permissions;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SystemServiceControllerImpl implements SystemServiceController {

    private final Context context;
    private final SettingsRepository settingsRepository;

    private final PublishSubject<Boolean> stopForegroundSubject = PublishSubject.create();

    private static final Handler stopHandler = new Handler(Looper.getMainLooper());

    public static void startPlayForegroundService(Context context) {
        startPlayForegroundService(context, 0);
    }

    public static void startPlayForegroundService(Context context, long playDelay) {
        stopHandler.removeCallbacksAndMessages(null);

        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra(MusicService.START_FOREGROUND_SIGNAL, true);
        intent.putExtra(MusicService.REQUEST_CODE, Constants.Actions.PLAY);
        intent.putExtra(MusicService.PLAY_DELAY_MILLIS, playDelay);
        checkPermissionsAndStartServiceFromBg(context, intent);
    }

    public SystemServiceControllerImpl(Context context,
                                       SettingsRepository settingsRepository) {
        this.context = context;
        this.settingsRepository = settingsRepository;
    }

    @Override
    public void startMusicService() {
        stopHandler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(context, MusicService.class);
        checkPermissionsAndStartServiceSafe(context, intent);
    }

    @Override
    public void stopMusicService(boolean forceStop, boolean hideUi) {
        long stopDelayMillis = settingsRepository.getKeepNotificationTime();
        if (forceStop || stopDelayMillis == 0L) {
            stopForegroundService(hideUi);
            return;
        }
        stopHandler.postDelayed(() -> stopForegroundService(hideUi), stopDelayMillis);
    }

    @NonNull
    @Override
    public Observable<Boolean> getStopForegroundSignal() {
        return stopForegroundSubject;
    }

    private static void checkPermissionsAndStartServiceSafe(Context context, Intent intent) {
        if (!Permissions.hasFilePermission(context)) {
            Components.getAppComponent()
                    .notificationsDisplayer()
                    .showErrorNotification(R.string.no_file_permission);
            return;
        }
        startServiceSafe(context, intent);
    }

    private static void checkPermissionsAndStartServiceFromBg(Context context, Intent intent) {
        if (!Permissions.hasFilePermission(context)) {
            Components.getAppComponent()
                    .notificationsDisplayer()
                    .showErrorNotification(R.string.no_file_permission);
            return;
        }
        startServiceFromBg(context, intent);
    }

    private static void startServiceSafe(Context context, Intent intent) {
        try {
            ServiceConnection connection = new ForegroundServiceStarterConnection(context, intent);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (RuntimeException ignored) {
            // Workaround for background calls
            startServiceFromBg(context, intent);
        }
    }

    private static void startServiceFromBg(Context context, Intent intent) {
        Intent bgIntent = new Intent(intent);
        intent.putExtra(MusicService.START_FOREGROUND_SIGNAL, true);
        try {
            ContextCompat.startForegroundService(context, bgIntent);
        } catch (Exception e) {
            if (processServiceStartError(context, e)) {
                return;
            }
            throw e;
        }
    }

    private static boolean processServiceStartError(Context context, Exception e) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e instanceof ForegroundServiceStartNotAllowedException) {
            AppComponent appComponent = Components.getAppComponent();
            appComponent.analytics().processNonFatalError(e);
            appComponent.notificationsDisplayer().showErrorNotification(R.string.app_has_no_system_permission_to_start);
            //check toast on this api version(S)
            Toast.makeText(context, R.string.app_has_no_system_permission_to_start, Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private void stopForegroundService(boolean hideUi) {
        stopForegroundSubject.onNext(hideUi);
    }

    private static class ForegroundServiceStarterConnection implements ServiceConnection {

        private final Context context;
        private final Intent intent;

        public ForegroundServiceStarterConnection(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) iBinder;
            MusicService service = binder.getService();
            try {
                ContextCompat.startForegroundService(context, intent);
            } catch (Exception e) {
                if (processServiceStartError(context, e)) {
                    context.unbindService(this);
                    return;
                }
                throw e;
            }
            service.startForeground();
            context.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    }

}
