package com.github.anrimian.simplemusicplayer.di.app;


import com.github.anrimian.simplemusicplayer.di.app.library.LibraryComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.LibraryModule;
import com.github.anrimian.simplemusicplayer.infrastructure.service.ServiceManager;
import com.github.anrimian.simplemusicplayer.infrastructure.service.music.MusicService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created on 11.02.2017.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        SchedulerModule.class,
        ErrorModule.class,
        MusicModule.class,
        DbModule.class,
        StorageModule.class,
        SettingsModule.class
})
public interface AppComponent {

    LibraryComponent libraryComponent(LibraryModule libraryModule);
    ServiceManager serviceManager();

    void inject(MusicService musicService);
}