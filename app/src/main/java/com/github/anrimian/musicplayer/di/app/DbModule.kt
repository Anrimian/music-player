package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.database.ConfigsDatabase;
import com.github.anrimian.musicplayer.data.database.DatabaseManager;
import com.github.anrimian.musicplayer.data.database.LibraryDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListDao;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 20.11.2017.
 */

@Module
public class DbModule {

    @Provides
    @Nonnull
    @Singleton
    DatabaseManager provideDatabaseManager(Context context) {
        return new DatabaseManager(context);
    }

    @Provides
    @Nonnull
    @Singleton
    LibraryDatabase provideAppDatabase(DatabaseManager databaseManager) {
        return databaseManager.getLibraryDatabase();
    }

    @Provides
    @Nonnull
    @Singleton
    PlayQueueDao playQueueDao(LibraryDatabase libraryDatabase) {
        return libraryDatabase.playQueueDao();
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionsDao compositionsDao(LibraryDatabase libraryDatabase) {
        return libraryDatabase.compositionsDao();
    }

    @Provides
    @Nonnull
    @Singleton
    PlayQueueDaoWrapper playQueueDaoWrapper(LibraryDatabase libraryDatabase, PlayQueueDao playQueueDao) {
        return new PlayQueueDaoWrapper(libraryDatabase, playQueueDao);
    }

    @Provides
    @Nonnull
    @Singleton
    ArtistsDao artistsDao(LibraryDatabase libraryDatabase) {
        return libraryDatabase.artistsDao();
    }

    @Provides
    @Nonnull
    @Singleton
    AlbumsDao albumsDao(LibraryDatabase libraryDatabase) {
        return libraryDatabase.albumsDao();
    }

    @Provides
    @Nonnull
    @Singleton
    GenreDao genreDao(LibraryDatabase libraryDatabase) {
        return libraryDatabase.genreDao();
    }

    @Provides
    @Nonnull
    @Singleton
    AlbumsDaoWrapper albumsDaoWrapper(LibraryDatabase libraryDatabase,
                                      AlbumsDao albumsDao,
                                      ArtistsDao artistsDao,
                                      ArtistsDaoWrapper artistsDaoWrapper) {
        return new AlbumsDaoWrapper(libraryDatabase, albumsDao, artistsDao, artistsDaoWrapper);
    }

    @Provides
    @Nonnull
    @Singleton
    ArtistsDaoWrapper artistsDaoWrapper(LibraryDatabase libraryDatabase,
                                        ArtistsDao artistsDao,
                                        AlbumsDao albumsDao) {
        return new ArtistsDaoWrapper(libraryDatabase, artistsDao, albumsDao);
    }

    @Provides
    @Nonnull
    @Singleton
    GenresDaoWrapper genresDaoWrapper(LibraryDatabase libraryDatabase,
                                      GenreDao genreDao,
                                      CompositionsDao compositionsDao) {
        return new GenresDaoWrapper(libraryDatabase, genreDao, compositionsDao);
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionsDaoWrapper compositionsDaoWrapper(LibraryDatabase libraryDatabase,
                                                  ArtistsDao artistsDao,
                                                  CompositionsDao compositionsDao,
                                                  AlbumsDao albumsDao,
                                                  GenreDao genreDao,
                                                  FoldersDao foldersDao) {
        return new CompositionsDaoWrapper(libraryDatabase,
                artistsDao,
                compositionsDao,
                albumsDao,
                genreDao,
                foldersDao);
    }

    @Provides
    @Nonnull
    @Singleton
    FoldersDao foldersDao(LibraryDatabase libraryDatabase) {
        return libraryDatabase.foldersDao();
    }

    @Provides
    @Nonnull
    @Singleton
    FoldersDaoWrapper foldersDaoWrapper(LibraryDatabase libraryDatabase,
                                        FoldersDao foldersDao,
                                        CompositionsDaoWrapper compositionsDao) {
        return new FoldersDaoWrapper(libraryDatabase, foldersDao, compositionsDao);
    }

    @Provides
    @Nonnull
    @Singleton
    PlayListDao playListDao(LibraryDatabase libraryDatabase) {
        return libraryDatabase.playListDao();
    }

    @Provides
    @Nonnull
    @Singleton
    PlayListsDaoWrapper playListsDaoWrapper(PlayListDao playListDao,
                                            CompositionsDao compositionsDao,
                                            LibraryDatabase libraryDatabase) {
        return new PlayListsDaoWrapper(playListDao, compositionsDao, libraryDatabase);
    }

    @Provides
    @Nonnull
    @Singleton
    ConfigsDatabase configsDatabase(DatabaseManager databaseManager) {
        return databaseManager.getConfigsDatabase();
    }

    @Provides
    @Nonnull
    @Singleton
    IgnoredFoldersDao ignoredFoldersDao(ConfigsDatabase configsDatabase) {
        return configsDatabase.ignoredFoldersDao();
    }

}
