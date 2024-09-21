package com.github.anrimian.musicplayer.di.app.library

import com.github.anrimian.musicplayer.di.app.library.albums.AlbumsComponent
import com.github.anrimian.musicplayer.di.app.library.albums.AlbumsModule
import com.github.anrimian.musicplayer.di.app.library.artists.ArtistsComponent
import com.github.anrimian.musicplayer.di.app.library.artists.ArtistsModule
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsComponent
import com.github.anrimian.musicplayer.di.app.library.compositions.LibraryCompositionsModule
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesComponent
import com.github.anrimian.musicplayer.di.app.library.files.LibraryFilesModule
import com.github.anrimian.musicplayer.di.app.library.genres.GenresComponent
import com.github.anrimian.musicplayer.di.app.library.genres.GenresModule
import com.github.anrimian.musicplayer.ui.player_screen.PlayerPresenter
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.LyricsPresenter
import com.github.anrimian.musicplayer.ui.player_screen.queue.PlayQueuePresenter
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersPresenter
import dagger.Subcomponent

/**
 * Created on 29.10.2017.
 */
@LibraryScope
@Subcomponent(modules = [ LibraryModule::class ])
interface LibraryComponent {
    
    fun libraryFilesComponent(module: LibraryFilesModule): LibraryFilesComponent
    fun libraryCompositionsComponent(module: LibraryCompositionsModule): LibraryCompositionsComponent
    fun artistsComponent(module: ArtistsModule): ArtistsComponent
    fun albumsComponent(module: AlbumsModule): AlbumsComponent
    fun genresComponent(module: GenresModule): GenresComponent

    fun playerPresenter(): PlayerPresenter
    fun playQueuePresenter(): PlayQueuePresenter
    fun lyricsPresenter(): LyricsPresenter
    fun excludedFoldersPresenter(): ExcludedFoldersPresenter
    
}
