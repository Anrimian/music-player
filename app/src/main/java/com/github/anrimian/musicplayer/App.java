package com.github.anrimian.musicplayer;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.github.anrimian.musicplayer.data.utils.Permissions;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.utils.rx.RxJavaErrorConsumer;
import com.github.anrimian.musicplayer.utils.DevTools;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * Created on 20.10.2017.
 */

public abstract class App extends Application {

    public App() {
        super();
        RxJavaPlugins.setErrorHandler(new RxJavaErrorConsumer());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        initComponents(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DevTools.run(this);

        AppComponent appComponent = Components.getAppComponent();
        appComponent.appLogger().initFatalErrorRecorder();

        if (Permissions.hasFilePermission(this)
                && !appComponent.loggerRepository().wasCriticalFatalError()
        ) {
            appComponent.widgetUpdater().start();
            appComponent.mediaScannerRepository().runStorageObserver();
        }
    }

    protected abstract void initComponents(Context appContext);

}
