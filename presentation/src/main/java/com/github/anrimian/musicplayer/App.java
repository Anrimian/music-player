package com.github.anrimian.musicplayer;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.github.anrimian.acrareportdialog.AcraReportDialog;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.utils.rx.RxJavaErrorConsumer;
import com.github.anrimian.musicplayer.utils.Permissions;

import io.reactivex.plugins.RxJavaPlugins;

/**
 * Created on 20.10.2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.setErrorHandler(new RxJavaErrorConsumer());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Components.init(getApplicationContext());

        if (BuildConfig.DEBUG) {
            AcraReportDialog.setupCrashDialog(this);
        }

        AppComponent appComponent = Components.getAppComponent();
        if (Permissions.hasFilePermission(this)) {
            appComponent.widgetUpdater().start();
            appComponent.mediaStorageRepository().initialize();
        }
    }
}
