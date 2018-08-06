package com.github.anrimian.simplemusicplayer.di.app;


import com.github.anrimian.simplemusicplayer.di.app.library.LibraryComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.LibraryModule;
import com.github.anrimian.simplemusicplayer.di.app.playlists.PlayListsComponent;
import com.github.anrimian.simplemusicplayer.di.app.playlists.PlayListsModule;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicServiceManager;
import com.github.anrimian.simplemusicplayer.infrastructure.service.music.MusicService;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.PlayListsPresenter;

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
    PlayListsComponent playListsComponent(PlayListsModule playListsModule);
    MusicServiceManager serviceManager();

    void inject(MusicService musicService);
}