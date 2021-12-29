package com.github.anrimian.musicplayer.lite.di;

import android.content.Context;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppModule;
import com.github.anrimian.musicplayer.lite.di.app.DaggerLiteAppComponent;
import com.github.anrimian.musicplayer.lite.di.app.LiteAppComponent;

public class LiteComponents {

    private static LiteComponents instance;

    private final LiteAppComponent liteAppComponent;

    public static void init(Context appContext) {
        instance = new LiteComponents(appContext);
    }

    private static LiteComponents getInstance() {
        if (instance == null) {
            throw new IllegalStateException("components must be initialized first");
        }
        return instance;
    }

    private LiteComponents(Context appContext) {
        liteAppComponent = DaggerLiteAppComponent.builder()
                .appModule(new AppModule(appContext))
                .build();
        Components.init(liteAppComponent);
    }

}
