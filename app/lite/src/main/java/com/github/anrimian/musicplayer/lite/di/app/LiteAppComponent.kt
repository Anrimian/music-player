package com.github.anrimian.musicplayer.lite.di.app;

import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.di.app.AppModule;
import com.github.anrimian.musicplayer.di.app.DbModule;
import com.github.anrimian.musicplayer.di.app.MusicModule;
import com.github.anrimian.musicplayer.di.app.PlayListsModule;
import com.github.anrimian.musicplayer.di.app.SchedulerModule;
import com.github.anrimian.musicplayer.di.app.SettingsModule;
import com.github.anrimian.musicplayer.di.app.StorageModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        SchedulerModule.class,
        MusicModule.class,
        DbModule.class,
        StorageModule.class,
        SettingsModule.class,
        PlayListsModule.class,

        LiteAppModule.class
})
public interface LiteAppComponent extends AppComponent {
}
