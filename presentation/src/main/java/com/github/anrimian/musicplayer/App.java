package com.github.anrimian.musicplayer;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.utils.rx.RxJavaErrorConsumer;
import com.github.anrimian.musicplayer.utils.Permissions;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * Created on 20.10.2017.
 */

/*
In v0.9.3:
-option to choose player implementation priority
-finish Czech localization
-finish arabic localization(+slidr, swipe, icons, motion layout)
-glide memory optimization
-glide save on disc full resolution images
-return eq attach logic back?
-crash here (StorageCompositionsInserter.java:180)
-exo player sound gap on skip
-cleanup MusicService
-slidr and scroll gesture can't be performed while file scanner is running: constant ui update
*/
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.setErrorHandler(new RxJavaErrorConsumer());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Components.init(getApplicationContext());

//        DevTools.run(this);

        AppComponent appComponent = Components.getAppComponent();
        appComponent.appLogger().initFatalErrorRecorder();

        if (Permissions.hasFilePermission(this)
                && !appComponent.loggerRepository().wasCriticalFatalError()
        ) {
            appComponent.widgetUpdater().start();
            appComponent.mediaScannerRepository().runStorageObserver();
        }
    }
}
