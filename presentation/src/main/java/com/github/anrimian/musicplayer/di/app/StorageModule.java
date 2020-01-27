package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.edit.EditorRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.library.folders.CompositionFoldersCache;
import com.github.anrimian.musicplayer.data.repositories.library.folders.MusicFolderDataSource;
import com.github.anrimian.musicplayer.data.repositories.scanner.MediaScannerRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.files.FileManager;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.business.editor.EditorInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;

@Module
public class StorageModule {

    @Provides
    @Nonnull
    @Singleton
    StorageMusicProvider storageMusicProvider(Context context, StorageAlbumsProvider albumsProvider) {
        return new StorageMusicProvider(context, albumsProvider);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageArtistsProvider storageArtistProvider(Context context) {
        return new StorageArtistsProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageGenresProvider storageGenresProvider(Context context) {
        return new StorageGenresProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageAlbumsProvider storageAlbumsProvider(Context context) {
        return new StorageAlbumsProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    FileManager fileManager() {
        return new FileManager();
    }

    @Provides
    @Nonnull
    @Singleton
    StorageMusicDataSource storageMusicDataSource(StorageMusicProvider musicProvider,
                                                  FileManager fileManager,
                                                  CompositionsDaoWrapper compositionsDao,
                                                  GenresDaoWrapper genreDao,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new StorageMusicDataSource(musicProvider, compositionsDao, genreDao, fileManager, scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionFoldersCache compositionFoldersCache(CompositionsDaoWrapper compositionsDao) {
        return new CompositionFoldersCache(compositionsDao);
    }

    @Provides
    @Nonnull
    @Singleton
    MusicFolderDataSource musicFolderDataSource(CompositionFoldersCache storageMusicDataSource) {
        return new MusicFolderDataSource(storageMusicDataSource);
    }

    @Provides
    @Nonnull
    EditorRepository compositionEditorRepository(StorageMusicDataSource storageMusicDataSource,
                                                 CompositionsDaoWrapper compositionsDao,
                                                 AlbumsDaoWrapper albumsDao,
                                                 ArtistsDaoWrapper artistsDao,
                                                 GenresDaoWrapper genresDao,
                                                 StorageMusicProvider storageMusicProvider,
                                                 StorageGenresProvider storageGenresProvider,
                                                 StorageArtistsProvider storageArtistsProvider,
                                                 StorageAlbumsProvider storageAlbumsProvider,
                                                 @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new EditorRepositoryImpl(storageMusicDataSource,
                compositionsDao,
                albumsDao,
                artistsDao,
                genresDao,
                storageMusicProvider,
                storageGenresProvider,
                storageArtistsProvider,
                storageAlbumsProvider,
                scheduler);
    }

    @Provides
    @Nonnull
    EditorInteractor compositionEditorInteractor(EditorRepository editorRepository,
                                                 LibraryRepository musicProviderRepository) {
        return new EditorInteractor(editorRepository, musicProviderRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    MediaScannerRepository mediaScannerRepository(StorageMusicProvider musicProvider,
                                                  StoragePlayListsProvider playListsProvider,
                                                  StorageGenresProvider genresProvider,
                                                  CompositionsDaoWrapper compositionsDao,
                                                  PlayListsDaoWrapper playListsDao,
                                                  GenresDaoWrapper genresDao,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MediaScannerRepositoryImpl(musicProvider,
                playListsProvider,
                genresProvider,
                compositionsDao,
                playListsDao,
                genresDao,
                scheduler);
    }

}
