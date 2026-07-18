package com.github.anrimian.musicplayer.di.app

import android.content.Context
import com.github.anrimian.musicplayer.data.database.ConfigsDatabase
import com.github.anrimian.musicplayer.data.database.DatabaseManager
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistDao
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DbModule {

    @Provides
    @Singleton
    fun provideDatabaseManager(
        context: Context,
        systemAudioCatalogProvider: SystemAudioCatalogProvider,
    ) = DatabaseManager(context, systemAudioCatalogProvider)

    @Provides
    @Singleton
    fun provideAppDatabase(
        databaseManager: DatabaseManager
    ): LibraryDatabase = databaseManager.getLibraryDatabase()

    @Provides
    @Singleton
    fun playQueueDao(
        libraryDatabase: LibraryDatabase
    ): PlayQueueDao = libraryDatabase.playQueueDao()

    @Provides
    @Singleton
    fun compositionsDao(
        libraryDatabase: LibraryDatabase
    ): CompositionsDao = libraryDatabase.compositionsDao()

    @Provides
    @Singleton
    fun playQueueDaoWrapper(
        libraryDatabase: LibraryDatabase,
        playQueueDao: PlayQueueDao
    ) = PlayQueueDaoWrapper(libraryDatabase, playQueueDao)

    @Provides
    @Singleton
    fun artistsDao(
        libraryDatabase: LibraryDatabase
    ): ArtistsDao = libraryDatabase.artistsDao()

    @Provides
    @Singleton
    fun albumsDao(
        libraryDatabase: LibraryDatabase
    ): AlbumsDao = libraryDatabase.albumsDao()

    @Provides
    @Singleton
    fun genreDao(
        libraryDatabase: LibraryDatabase
    ): GenreDao = libraryDatabase.genreDao()

    @Provides
    @Singleton
    fun albumsDaoWrapper(
        libraryDatabase: LibraryDatabase,
        albumsDao: AlbumsDao,
        artistsDao: ArtistsDao,
        artistsDaoWrapper: ArtistsDaoWrapper
    ) = AlbumsDaoWrapper(libraryDatabase, albumsDao, artistsDao, artistsDaoWrapper)

    @Provides
    @Singleton
    fun artistsDaoWrapper(
        libraryDatabase: LibraryDatabase,
        artistsDao: ArtistsDao,
        albumsDao: AlbumsDao
    ) = ArtistsDaoWrapper(libraryDatabase, artistsDao, albumsDao)

    @Provides
    @Singleton
    fun genresDaoWrapper(
        libraryDatabase: LibraryDatabase,
        genreDao: GenreDao,
        compositionsDao: CompositionsDao
    ) = GenresDaoWrapper(libraryDatabase, genreDao, compositionsDao)

    @Provides
    @Singleton
    fun compositionsDaoWrapper(
        libraryDatabase: LibraryDatabase,
        artistsDao: ArtistsDao,
        artistsDaoWrapper: ArtistsDaoWrapper,
        compositionsDao: CompositionsDao,
        albumsDao: AlbumsDao,
        genreDao: GenreDao,
        foldersDao: FoldersDao
    ) = CompositionsDaoWrapper(
        libraryDatabase,
        artistsDao,
        artistsDaoWrapper,
        compositionsDao,
        albumsDao,
        genreDao,
        foldersDao
    )

    @Provides
    @Singleton
    fun foldersDao(
        libraryDatabase: LibraryDatabase
    ): FoldersDao = libraryDatabase.foldersDao()

    @Provides
    @Singleton
    fun foldersDaoWrapper(
        libraryDatabase: LibraryDatabase,
        foldersDao: FoldersDao,
        compositionsDao: CompositionsDaoWrapper,
    ) = FoldersDaoWrapper(libraryDatabase, foldersDao, compositionsDao)

    @Provides
    @Singleton
    fun playListDao(
        libraryDatabase: LibraryDatabase
    ): PlaylistDao = libraryDatabase.playListDao()

    @Provides
    @Singleton
    fun playListsDaoWrapper(
        playListDao: PlaylistDao,
        compositionsDao: CompositionsDao,
        libraryDatabase: LibraryDatabase
    ) = PlaylistsDaoWrapper(playListDao, compositionsDao, libraryDatabase)

    @Provides
    @Singleton
    fun configsDatabase(
        databaseManager: DatabaseManager
    ): ConfigsDatabase = databaseManager.getConfigsDatabase()

    @Provides
    @Singleton
    fun ignoredFoldersDao(
        configsDatabase: ConfigsDatabase
    ): IgnoredFoldersDao = configsDatabase.ignoredFoldersDao()
}
