package com.github.anrimian.musicplayer.infrastructure.service;

import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

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

import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.Constants;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.Permissions;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SystemServiceControllerImpl implements SystemServiceController {

    private final Context context;

    private final PublishSubject<Object> stopForegroundSubject = PublishSubject.create();

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void startPlayForegroundService(Context context) {
        startPlayForegroundService(context, 0);
    }

    public static void startPlayForegroundService(Context context, int playDelay) {
        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra(MusicService.START_FOREGROUND_SIGNAL, true);
        intent.putExtra(MusicService.REQUEST_CODE, Constants.Actions.PLAY);
        intent.putExtra(MusicService.PLAY_DELAY_MILLIS, playDelay);
        checkPermissionsAndStartServiceFromBg(context, intent);
    }

    public SystemServiceControllerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void startMusicService() {
        handler.post(() -> {
            Intent intent = new Intent(context, MusicService.class);
            checkPermissionsAndStartServiceSafe(context, intent);
        });
    }

    @Override
    public void stopMusicService() {
        handler.removeCallbacksAndMessages(null);
        stopForegroundSubject.onNext(TRIGGER);
    }

    @Override
    public Observable<Object> getStopForegroundSignal() {
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
        handler.post(() -> {
            try {
                ServiceConnection connection = new ForegroundServiceStarterConnection(context, intent);
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            } catch (RuntimeException ignored) {
                // Workaround for background calls
                startServiceFromBg(context, intent);
            }
        });
    }

    private static void startServiceFromBg(Context context, Intent intent) {
        Intent bgIntent = new Intent(intent);
        intent.putExtra(MusicService.START_FOREGROUND_SIGNAL, true);
        //let's see how try-catch will work
        try {
            ContextCompat.startForegroundService(context, bgIntent);
        } catch (Exception e) {
            int sdkVersion = Build.VERSION.SDK_INT;
            if (sdkVersion >= Build.VERSION_CODES.S && sdkVersion < Build.VERSION_CODES.TIRAMISU
                    && e instanceof ForegroundServiceStartNotAllowedException) {
                AppComponent appComponent = Components.getAppComponent();
                appComponent.analytics().processNonFatalError(e);
                appComponent.notificationsDisplayer().showErrorNotification(R.string.app_has_no_system_permission_to_start);
                //check toast on this api version(S)
                Toast.makeText(context, R.string.app_has_no_system_permission_to_start, Toast.LENGTH_LONG).show();
                return;
            }
            throw e;
        }
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
            ContextCompat.startForegroundService(context, intent);
            service.startForeground();
            context.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    }

}
