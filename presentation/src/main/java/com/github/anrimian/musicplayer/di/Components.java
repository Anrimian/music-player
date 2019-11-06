package com.github.anrimian.musicplayer.di;


import android.content.Context;

import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.di.app.AppModule;
import com.github.anrimian.musicplayer.di.app.DaggerAppComponent;
import com.github.anrimian.musicplayer.di.app.editor.CompositionEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.CompositionEditorModule;
import com.github.anrimian.musicplayer.di.app.library.LibraryComponent;
import com.github.anrimian.musicplayer.di.app.library.LibraryModule;
import com.github.anrimian.musicplayer.di.app.library.albums.AlbumsComponent;
import com.github.anrimian.musicplayer.di.app.library.albums.AlbumsModule;
import com.github.anrimian.musicplayer.di.app.library.artists.ArtistsComponent;
import com.github.anrimian.musicplayer.di.app.library.artists.ArtistsModule;
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsComponent;
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsModule;
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesComponent;
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesModule;
import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderComponent;
import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderModule;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListComponent;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListModule;
import com.github.anrimian.musicplayer.di.app.settings.SettingsComponent;

import javax.annotation.Nullable;


/**
 * Created on 11.02.2017.
 */

public class Components {

    private static Components instance;

    private AppComponent appComponent;
    private LibraryComponent libraryComponent;
    private LibraryFilesComponent libraryFilesComponent;

    public static void init(Context appContext) {
        instance = new Components(appContext);
    }

    private static Components getInstance() {
        if (instance == null) {
            throw new IllegalStateException("components must be initialized first");
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

    public static FolderComponent getLibraryFolderComponent(@Nullable String path) {
        return getLibraryRootFolderComponent().folderComponent(new FolderModule(path));
    }

    public static LibraryFilesComponent getLibraryRootFolderComponent() {
        return getInstance().buildLibraryFilesComponent();
    }

    public static LibraryCompositionsComponent getLibraryCompositionsComponent() {
        return getLibraryComponent().libraryCompositionsComponent(new LibraryCompositionsModule());
    }

    public static ArtistsComponent artistsComponent() {
        return getLibraryComponent().artistsComponent(new ArtistsModule());
    }

    public static AlbumsComponent albumsComponent() {
        return getLibraryComponent().albumsComponent(new AlbumsModule());
    }

    public static PlayListComponent getPlayListComponent(long playListId) {
        return getAppComponent().playListComponent(new PlayListModule(playListId));
    }

    public static CompositionEditorComponent getCompositionEditorComponent(long compositionId) {
        return getAppComponent().compositionEditorComponent(
                new CompositionEditorModule(compositionId)
        );
    }

    public static SettingsComponent getSettingsComponent() {
        return getAppComponent().settingsComponent();
    }

    private LibraryComponent buildLibraryComponent() {
        if (libraryComponent == null) {
            libraryComponent = getAppComponent().libraryComponent(new LibraryModule());
        }
        return libraryComponent;
    }

    private LibraryFilesComponent buildLibraryFilesComponent() {
        if (libraryFilesComponent == null) {
            libraryFilesComponent = getLibraryComponent().libraryFilesComponent(new LibraryFilesModule());
        }
        return libraryFilesComponent;
    }

    private AppComponent buildAppComponent(Context appContext) {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(appContext))
                .build();
    }

}
