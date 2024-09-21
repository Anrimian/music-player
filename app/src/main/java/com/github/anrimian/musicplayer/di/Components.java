package com.github.anrimian.musicplayer.di;


import android.content.Context;

import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorModule;
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorModule;
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorModule;
import com.github.anrimian.musicplayer.di.app.editor.genre.GenreEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.genre.GenreEditorModule;
import com.github.anrimian.musicplayer.di.app.editor.lyrics.LyricsEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.lyrics.LyricsEditorModule;
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerComponent;
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerModule;
import com.github.anrimian.musicplayer.di.app.library.LibraryComponent;
import com.github.anrimian.musicplayer.di.app.library.LibraryModule;
import com.github.anrimian.musicplayer.di.app.library.albums.AlbumsComponent;
import com.github.anrimian.musicplayer.di.app.library.albums.AlbumsModule;
import com.github.anrimian.musicplayer.di.app.library.albums.items.AlbumItemsComponent;
import com.github.anrimian.musicplayer.di.app.library.albums.items.AlbumItemsModule;
import com.github.anrimian.musicplayer.di.app.library.artists.ArtistsComponent;
import com.github.anrimian.musicplayer.di.app.library.artists.ArtistsModule;
import com.github.anrimian.musicplayer.di.app.library.artists.items.ArtistItemsComponent;
import com.github.anrimian.musicplayer.di.app.library.artists.items.ArtistItemsModule;
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsComponent;
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsModule;
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesComponent;
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesModule;
import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderComponent;
import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderModule;
import com.github.anrimian.musicplayer.di.app.library.genres.GenresComponent;
import com.github.anrimian.musicplayer.di.app.library.genres.GenresModule;
import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsComponent;
import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsModule;
import com.github.anrimian.musicplayer.di.app.order.OrderComponent;
import com.github.anrimian.musicplayer.di.app.order.OrderModule;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListComponent;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListModule;
import com.github.anrimian.musicplayer.di.app.settings.SettingsComponent;
import com.github.anrimian.musicplayer.di.app.share.ShareComponent;
import com.github.anrimian.musicplayer.di.app.share.ShareModule;
import com.github.anrimian.musicplayer.domain.models.order.Order;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;


/**
 * Created on 11.02.2017.
 */

public class Components {

    private static Components instance;

    private final AppComponent appComponent;
    private LibraryComponent libraryComponent;
    private LibraryFilesComponent libraryFilesComponent;

    public static void init(AppComponent appComponent) {
        instance = new Components(appComponent);
    }

    /*
     * Experiment:
     * After refactor MainActivity to kt crashes started to appear: uninitialized components here
     *  1) Moved this method to base java class
     *     Observe how it works
     *      - has no effect
     *  2) Moved components initialization to Application.attachBaseContext
     *     Based on answer https://stackoverflow.com/a/56676594/5541688
     *     Observe how it works - doesn't work
     *  3) Possible next option: separate singleton and builder for locale controller
     *     Implemented separate initialization for this case
     *     Observe how it works - crashes later
     *  4) Initialize here if not initialized - observe how it works
     *     Doesn't work either
     *  5) Add to proguard rule to keep getInstance() in LiteComponents
     *     If it helps - remove initialization from attempt 4
     *     Issue: can't find method init()
     *  5) Remake LiteComponents to kotlin object
     *     If works: try to remote reflection initializer(with r8 rules?); copy approach to SyncComponents;
     *     No, doesn't work
     *  5.1) Fixed reflection initializer
     *  Spotted crashes in AppWidgets(after system restart) and in MediaBrowserService
     */
    public static void checkInitialization(Context appContext) {
        if (!Components.isInitialized()) {
            try {
                Class<?> clazz;
                try {
                    clazz = Class.forName("com.github.anrimian.musicplayer.lite.di.LiteComponents");
                } catch (ClassNotFoundException e) {
                    clazz = Class.forName("com.github.anrimian.musicplayer.sync.di.SyncComponents");
                }
                // looking for method init(Context)
                Method method = null;
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(Context.class)) {
                        method = m;
                        break;
                    }
                }
                if (method == null) {
                    throw new NoSuchMethodException();
                }
                Field[] fields = clazz.getDeclaredFields();
                Object instance = null;
                for (Field field : fields) {
                    if (Modifier.isFinal(field.getModifiers())) {
                        instance = field.get(null);
                    }
                }
                if (instance == null) {
                    throw new NoSuchFieldException();
                }
                method.invoke(instance, appContext);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Components getInstance() {
        if (instance == null) {
            throw new IllegalStateException("components must be initialized first");
        }
        return instance;
    }

    private Components(AppComponent appComponent) {
        this.appComponent = appComponent;
    }

    public static AppComponent getAppComponent() {
        return getInstance().appComponent;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static LibraryComponent getLibraryComponent() {
        return getInstance().buildLibraryComponent();
    }

    public static FolderComponent getLibraryFolderComponent(@Nullable Long folderId) {
        return getLibraryRootFolderComponent().folderComponent(new FolderModule(folderId));
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

    public static ArtistItemsComponent artistItemsComponent(long artistId) {
        return artistsComponent().artistItemsComponent(new ArtistItemsModule(artistId));
    }

    public static AlbumsComponent albumsComponent() {
        return getLibraryComponent().albumsComponent(new AlbumsModule());
    }

    public static AlbumItemsComponent albumItemsComponent(long albumId) {
        return albumsComponent().albumItemsComponent(new AlbumItemsModule(albumId));
    }

    public static GenresComponent genresComponent() {
        return getLibraryComponent().genresComponent(new GenresModule());
    }

    public static GenreItemsComponent genreItemsComponent(long genreId) {
        return genresComponent().genreItemsComponent(new GenreItemsModule(genreId));
    }

    public static PlayListComponent getPlayListComponent(long playListId) {
        return getAppComponent().playListComponent(new PlayListModule(playListId));
    }

    public static CompositionEditorComponent getCompositionEditorComponent(long compositionId) {
        return getAppComponent().compositionEditorComponent(
                new CompositionEditorModule(compositionId)
        );
    }

    public static AlbumEditorComponent getAlbumEditorComponent(long albumId) {
        return getAppComponent().albumEditorComponent(new AlbumEditorModule(albumId));
    }

    public static ArtistEditorComponent getArtistEditorComponent(long artistId, String name) {
        return getAppComponent().artistEditorComponent(new ArtistEditorModule(artistId, name));
    }

    public static GenreEditorComponent getGenreEditorComponent(long genreId, String name) {
        return getAppComponent().genreEditorComponent(new GenreEditorModule(genreId, name));
    }

    public static LyricsEditorComponent getLyricsEditorComponent(long genreId) {
        return getAppComponent().lyricsEditorComponent(new LyricsEditorModule(genreId));
    }

    public static SettingsComponent getSettingsComponent() {
        return getAppComponent().settingsComponent();
    }

    public static ExternalPlayerComponent getExternalPlayerComponent() {
        return getAppComponent().externalPlayerComponent(new ExternalPlayerModule());
    }

    public static ShareComponent getShareComponent(long[] ids) {
        return getAppComponent().shareComponent(new ShareModule(ids));
    }

    public static OrderComponent getOrderComponent(Order order) {
        return getAppComponent().orderComponent(new OrderModule(order));
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

}
