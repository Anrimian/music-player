package com.github.anrimian.simplemusicplayer.di;


import android.content.Context;

import com.github.anrimian.simplemusicplayer.di.app.AppComponent;
import com.github.anrimian.simplemusicplayer.di.app.AppModule;
import com.github.anrimian.simplemusicplayer.di.app.DaggerAppComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.LibraryComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.LibraryModule;
import com.github.anrimian.simplemusicplayer.di.app.library.compositions.LibraryCompositionsComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.compositions.LibraryCompositionsModule;
import com.github.anrimian.simplemusicplayer.di.app.library.files.LibraryFilesComponent;
import com.github.anrimian.simplemusicplayer.di.app.library.files.LibraryFilesModule;
import com.github.anrimian.simplemusicplayer.di.app.play_list.PlayListComponent;
import com.github.anrimian.simplemusicplayer.di.app.play_list.PlayListModule;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import javax.annotation.Nullable;


/**
 * Created on 11.02.2017.
 */

public class Components {

    private static Components instance;

    private AppComponent appComponent;
    private LibraryComponent libraryComponent;

    public static void init(Context appContext) {
        instance = new Components(appContext);
    }

    private static Components getInstance() {
        if (instance == null) {
            throw new IllegalStateException("components must be init first");
        }
        return instance;
    }

    private Components(Context appContext) {
        appComponent = buildAppComponent(appContext);
    }

    public static AppComponent getAppComponent() {
        return getInstance().appComponent;
    }

    public static LibraryComponent getLibraryComponent() {
        return getInstance().buildLibraryComponent();
    }

    public static LibraryFilesComponent getLibraryFilesComponent(@Nullable String path) {
        return getLibraryComponent().libraryFilesComponent(new LibraryFilesModule(path));
    }

    public static LibraryCompositionsComponent getLibraryCompositionsComponent() {
        return getLibraryComponent().libraryCompositionsComponent(new LibraryCompositionsModule());
    }

    public static PlayListComponent getPlayListComponent(long playListId) {
        return getAppComponent().playListComponent(new PlayListModule(playListId));
    }

    private LibraryComponent buildLibraryComponent() {
        if (libraryComponent == null) {
            libraryComponent = getAppComponent().libraryComponent(new LibraryModule());
        }
        return libraryComponent;
    }

    private AppComponent buildAppComponent(Context appContext) {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(appContext))
                .build();
    }

}
