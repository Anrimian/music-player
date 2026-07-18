package com.github.anrimian.musicplayer.di

import android.content.Context
import com.github.anrimian.musicplayer.di.app.AppComponent
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorModule
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorModule
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorModule
import com.github.anrimian.musicplayer.di.app.editor.genre.GenreEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.genre.GenreEditorModule
import com.github.anrimian.musicplayer.di.app.editor.lyrics.LyricsEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.lyrics.LyricsEditorModule
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerComponent
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerModule
import com.github.anrimian.musicplayer.di.app.library.LibraryComponent
import com.github.anrimian.musicplayer.di.app.library.albums.AlbumsModule
import com.github.anrimian.musicplayer.di.app.library.albums.items.AlbumItemsComponent
import com.github.anrimian.musicplayer.di.app.library.albums.items.AlbumItemsModule
import com.github.anrimian.musicplayer.di.app.library.artists.ArtistsModule
import com.github.anrimian.musicplayer.di.app.library.artists.items.ArtistItemsComponent
import com.github.anrimian.musicplayer.di.app.library.artists.items.ArtistItemsModule
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsComponent
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsModule
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesComponent
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesModule
import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderComponent
import com.github.anrimian.musicplayer.di.app.library.files.folder.FolderModule
import com.github.anrimian.musicplayer.di.app.library.genres.GenresModule
import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsComponent
import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsModule
import com.github.anrimian.musicplayer.di.app.order.OrderComponent
import com.github.anrimian.musicplayer.di.app.order.OrderModule
import com.github.anrimian.musicplayer.di.app.play_list.PlayListComponent
import com.github.anrimian.musicplayer.di.app.play_list.PlayListModule
import com.github.anrimian.musicplayer.di.app.share.ShareComponent
import com.github.anrimian.musicplayer.di.app.share.ShareModule
import com.github.anrimian.musicplayer.domain.models.order.Order
import java.lang.reflect.Modifier

object Components {

    private lateinit var appComponent: AppComponent
    private lateinit var libraryComponent: LibraryComponent
    private lateinit var libraryFilesComponent: LibraryFilesComponent

    fun init(appComponent: AppComponent) {
        this.appComponent = appComponent
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
    fun checkInitialization(appContext: Context) {
        if (!::appComponent.isInitialized) {
            try {
                val clazz = try {
                    Class.forName("com.github.anrimian.musicplayer.lite.di.LiteComponents")
                } catch (_: ClassNotFoundException) {
                    Class.forName("com.github.anrimian.musicplayer.sync.di.SyncComponents")
                }
                // looking for method init(Context)
                var method: java.lang.reflect.Method? = null
                for (m in clazz.declaredMethods) {
                    if (m.parameterCount == 1 && m.parameterTypes[0] == Context::class.java) {
                        method = m
                        break
                    }
                }
                if (method == null) {
                    throw NoSuchMethodException()
                }
                val fields = clazz.declaredFields
                var instance: Any? = null
                for (field in fields) {
                    if (Modifier.isFinal(field.modifiers)) {
                        instance = field.get(null)
                    }
                }
                if (instance == null) {
                    throw NoSuchFieldException()
                }
                method.invoke(instance, appContext)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    fun getAppComponent(): AppComponent {
        check(::appComponent.isInitialized) { "components must be initialized first" }
        return appComponent
    }


    fun getLibraryComponent() = buildLibraryComponent()

    fun getLibraryFolderComponent(folderId: Long): FolderComponent {
        return getLibraryRootFolderComponent().folderComponent(FolderModule(folderId))
    }

    fun getLibraryRootFolderComponent() = buildLibraryFilesComponent()

    fun getLibraryCompositionsComponent(): LibraryCompositionsComponent {
        return getLibraryComponent().libraryCompositionsComponent(LibraryCompositionsModule())
    }

    fun artistsComponent() = getLibraryComponent().artistsComponent(ArtistsModule())

    fun artistItemsComponent(artistId: Long): ArtistItemsComponent {
        return artistsComponent().artistItemsComponent(ArtistItemsModule(artistId))
    }

    fun albumsComponent() = getLibraryComponent().albumsComponent(AlbumsModule())

    fun albumItemsComponent(albumId: Long): AlbumItemsComponent {
        return albumsComponent().albumItemsComponent(AlbumItemsModule(albumId))
    }

    fun genresComponent() = getLibraryComponent().genresComponent(GenresModule())

    fun genreItemsComponent(genreId: Long): GenreItemsComponent {
        return genresComponent().genreItemsComponent(GenreItemsModule(genreId))
    }

    fun getPlayListComponent(playListId: Long): PlayListComponent {
        return getAppComponent().playListComponent(PlayListModule(playListId))
    }

    fun getCompositionEditorComponent(compositionId: Long): CompositionEditorComponent {
        return getAppComponent().compositionEditorComponent(CompositionEditorModule(compositionId))
    }

    fun getAlbumEditorComponent(albumId: Long): AlbumEditorComponent {
        return getAppComponent().albumEditorComponent(AlbumEditorModule(albumId))
    }

    fun getArtistEditorComponent(artistId: Long, name: String): ArtistEditorComponent {
        return getAppComponent().artistEditorComponent(ArtistEditorModule(artistId, name))
    }

    fun getGenreEditorComponent(genreId: Long, name: String): GenreEditorComponent {
        return getAppComponent().genreEditorComponent(GenreEditorModule(genreId, name))
    }

    fun getLyricsEditorComponent(genreId: Long): LyricsEditorComponent {
        return getAppComponent().lyricsEditorComponent(LyricsEditorModule(genreId))
    }

    fun getSettingsComponent() = getAppComponent().settingsComponent()

    fun getExternalPlayerComponent(): ExternalPlayerComponent {
        return getAppComponent().externalPlayerComponent(ExternalPlayerModule())
    }

    fun getShareComponent(ids: LongArray): ShareComponent {
        return getAppComponent().shareComponent(ShareModule(ids))
    }

    fun getOrderComponent(order: Order): OrderComponent {
        return getAppComponent().orderComponent(OrderModule(order))
    }

    private fun buildLibraryComponent(): LibraryComponent {
        if (!::libraryComponent.isInitialized) {
            libraryComponent = getAppComponent().libraryComponent()
        }
        return libraryComponent
    }

    private fun buildLibraryFilesComponent(): LibraryFilesComponent {
        if (!::libraryFilesComponent.isInitialized) {
            libraryFilesComponent = getLibraryComponent().libraryFilesComponent(LibraryFilesModule())
        }
        return libraryFilesComponent
    }
}
