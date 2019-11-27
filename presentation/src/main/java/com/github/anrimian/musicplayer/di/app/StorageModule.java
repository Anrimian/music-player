package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.music.edit.EditorRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.music.folders.CompositionFoldersCache;
import com.github.anrimian.musicplayer.data.repositories.music.folders.MusicFolderDataSource;
import com.github.anrimian.musicplayer.data.storage.files.FileManager;
import com.github.anrimian.musicplayer.data.storage.providers.MediaStorageRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.business.editor.CompositionEditorInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaStorageRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

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
    StorageMusicProvider storageMusicProvider(Context context) {
        return new StorageMusicProvider(context);
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
                                                 @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new EditorRepositoryImpl(storageMusicDataSource, scheduler);
    }

    @Provides
    @Nonnull
    CompositionEditorInteractor compositionEditorInteractor(EditorRepository editorRepository,
                                                            MusicProviderRepository musicProviderRepository) {
        return new CompositionEditorInteractor(editorRepository, musicProviderRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    MediaStorageRepository mediaStorageRepository(StorageMusicProvider musicProvider,
                                                  StoragePlayListsProvider playListsProvider,
                                                  StorageArtistsProvider artistsProvider,
                                                  StorageAlbumsProvider albumsProvider,
                                                  StorageGenresProvider genresProvider,
                                                  CompositionsDaoWrapper compositionsDao,
                                                  PlayListsDaoWrapper playListsDao,
                                                  ArtistsDaoWrapper artistsDao,
                                                  AlbumsDaoWrapper albumsDao,
                                                  GenresDaoWrapper genresDao,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MediaStorageRepositoryImpl(musicProvider,
                playListsProvider,
                artistsProvider,
                albumsProvider,
                genresProvider,
                compositionsDao,
                playListsDao,
                artistsDao,
                albumsDao,
                genresDao,
                scheduler);
    }

}
