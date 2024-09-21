package com.github.anrimian.musicplayer.ui.common.activity;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.github.anrimian.musicplayer.di.Components;


public class BaseAppCompatActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        // should be always initialized here, but bug is somewhere in Android sdk
        // so initialize manually in this case
        Components.checkInitialization(base.getApplicationContext());
        Context localizedContext = Components.getAppComponent()
                .localeController()
                .dispatchAttachBaseContext(base);
        super.attachBaseContext(localizedContext);
    }

}
