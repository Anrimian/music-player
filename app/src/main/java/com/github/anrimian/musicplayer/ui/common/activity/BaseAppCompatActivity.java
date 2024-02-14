package com.github.anrimian.musicplayer.ui.common.activity;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.github.anrimian.musicplayer.di.Components;

public class BaseAppCompatActivity extends AppCompatActivity {

    /*
     * Experiment:
     * After refactor MainActivity to kt crashes started to appear: uninitialized components here
     *  Moved this method to base java class
     *  Observe how it works
     *   - has no effect
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Components.getAppComponent().localeController().dispatchAttachBaseContext(base));
    }
}
