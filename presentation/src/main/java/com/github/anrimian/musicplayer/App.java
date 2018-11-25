package com.github.anrimian.musicplayer;

import android.app.Application;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.utils.acra.AcraReportDialog;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.config.ConfigurationBuilder;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Created on 20.10.2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Components.init(getApplicationContext());

        if (BuildConfig.DEBUG) {
            initAcra();
        }
    }

    private void initAcra() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(this);
//        if (BuildConfig.DEBUG) {
            configurationBuilder
                    .setReportDialogClass(AcraReportDialog.class)
                    .setReportingInteractionMode(ReportingInteractionMode.DIALOG);
//        }
        ACRA.init(this, configurationBuilder);
    }
}
