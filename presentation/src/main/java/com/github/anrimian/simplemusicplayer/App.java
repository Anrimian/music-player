package com.github.anrimian.simplemusicplayer;

import android.app.Application;

import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.utils.acra.AcraReportDialog;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.config.ConfigurationBuilder;

/**
 * Created on 20.10.2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Components.init(getApplicationContext());
        initAcra();
    }

    private void initAcra() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(this);
        //if (BuildConfig.DEBUG) {
        configurationBuilder
                .setReportDialogClass(AcraReportDialog.class)
                .setReportingInteractionMode(ReportingInteractionMode.DIALOG);
        //}
        ACRA.init(this, configurationBuilder);
    }
}
