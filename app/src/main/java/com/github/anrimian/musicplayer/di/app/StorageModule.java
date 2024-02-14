package com.github.anrimian.musicplayer.di.app;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.SLOW_BG_SCHEDULER;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.github.anrimian.filesync.SyncInteractor;
import com.github.anrimian.musicplayer.data.database.LibraryDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.edit.EditorRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.scanner.MediaScannerRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.scanner.StorageCompositionAnalyzer;
import com.github.anrimian.musicplayer.data.repositories.scanner.files.FileScanner;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.PlaylistFilesStorage;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.StoragePlaylistsAnalyzer;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSourceApi30;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSourceImpl;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor;
import com.github.anrimian.musicplayer.data.storage.source.ContentSourceHelper;
import com.github.anrimian.musicplayer.data.storage.source.FileSourceProvider;
import com.github.anrimian.musicplayer.data.storage.source.StorageSourceRepositoryImpl;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor;
import com.github.anrimian.musicplayer.domain.models.sync.FileKey;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

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
    FileSourceProvider fileSourceProvider(Context context) {
        return new FileSourceProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionSourceEditor compositionSourceEditor(StorageMusicProvider musicProvider,
                                                    FileSourceProvider fileSourceProvider,
                                                    ContentSourceHelper contentSourceHelper) {
        return new CompositionSourceEditor(musicProvider, fileSourceProvider, contentSourceHelper);
    }

    @Provides
    @Nonnull
    @Singleton
    EditorRepository compositionEditorRepository(CompositionSourceEditor sourceEditor,
                                                 StorageFilesDataSource filesDataSource,
                                                 LibraryDatabase libraryDatabase,
                                                 CompositionsDaoWrapper compositionsDao,
                                                 AlbumsDaoWrapper albumsDao,
                                                 ArtistsDaoWrapper artistsDao,
                                                 GenresDaoWrapper genresDao,
                                                 FoldersDaoWrapper foldersDao,
                                                 PlayListsDaoWrapper playListsDao,
                                                 StorageMusicProvider storageMusicProvider,
                                                 PlayListsRepository playListsRepository,
                                                 LibraryRepository libraryRepository,
                                                 @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new EditorRepositoryImpl(
                sourceEditor,
                filesDataSource,
                libraryDatabase,
                compositionsDao,
                albumsDao,
                artistsDao,
                genresDao,
                foldersDao,
                playListsDao,
                storageMusicProvider,
                playListsRepository,
                libraryRepository,
                scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageFilesDataSource storageFilesDataSource(StorageMusicProvider musicProvider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new StorageFilesDataSourceApi30(musicProvider);
        }
        return new StorageFilesDataSourceImpl(musicProvider);
    }

    @Provides
    @Nonnull
    EditorInteractor compositionEditorInteractor(CompositionSourceInteractor sourceInteractor,
                                                 SyncInteractor<FileKey, ?, Long> syncInteractor,
                                                 EditorRepository editorRepository,
                                                 LibraryRepository musicProviderRepository,
                                                 StorageSourceRepository storageSourceRepository) {
        return new EditorInteractor(
                sourceInteractor,
                syncInteractor,
                editorRepository,
                musicProviderRepository,
                storageSourceRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    FileScanner fileScanner(CompositionsDaoWrapper compositionsDao,
                            CompositionSourceEditor compositionSourceEditor,
                            StateRepository stateRepository,
                            StorageSourceRepository storageSourceRepository,
                            Analytics analytics,
                            @Named(SLOW_BG_SCHEDULER) Scheduler scheduler) {
        return new FileScanner(compositionsDao,
                compositionSourceEditor,
                stateRepository,
                storageSourceRepository,
                analytics,
                scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    MediaScannerRepository mediaScannerRepository(StorageMusicProvider musicProvider,
                                                  StoragePlayListsProvider playListsProvider,
                                                  CompositionsDaoWrapper compositionsDao,
                                                  StateRepository stateRepository,
                                                  SettingsRepository settingsRepository,
                                                  StorageCompositionAnalyzer compositionAnalyzer,
                                                  StoragePlaylistsAnalyzer storagePlaylistAnalyzer,
                                                  FileScanner fileScanner,
                                                  LoggerRepository loggerRepository,
                                                  Analytics analytics,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MediaScannerRepositoryImpl(musicProvider,
                playListsProvider,
                compositionsDao,
                stateRepository,
                settingsRepository,
                compositionAnalyzer,
                storagePlaylistAnalyzer,
                fileScanner,
                loggerRepository,
                analytics,
                scheduler);
    }

    @Provides
    @Nonnull
    StorageCompositionAnalyzer compositionAnalyzer(CompositionsDaoWrapper compositionsDao,
                                                   IgnoredFoldersDao ignoredFoldersDao,
                                                   StateRepository stateRepository,
                                                   StorageCompositionsInserter compositionsInserter) {
        return new StorageCompositionAnalyzer(
                compositionsDao,
                ignoredFoldersDao,
                stateRepository,
                compositionsInserter,
                Environment.getExternalStorageDirectory().getAbsolutePath()
        );
    }

    @Provides
    @Nonnull
    StoragePlaylistsAnalyzer storagePlaylistsAnalyzer2(CompositionsDaoWrapper compositionsDao,
                                                        PlayListsDaoWrapper playListsDao,
                                                        StoragePlayListsProvider playListsProvider,
                                                        PlaylistFilesStorage playlistFilesStorage) {
        return new StoragePlaylistsAnalyzer(compositionsDao, playListsDao, playListsProvider, playlistFilesStorage);
    }

    @Provides
    @Nonnull
    PlaylistFilesStorage playlistFilesStorage(Context context, Analytics analytics) {
        return new PlaylistFilesStorage(context, analytics);
    }

    @Provides
    @Nonnull
    StorageCompositionsInserter compositionsInserter(LibraryDatabase libraryDatabase,
                                                     CompositionsDao compositionsDao,
                                                     CompositionsDaoWrapper compositionsDaoWrapper,
                                                     FoldersDaoWrapper foldersDao,
                                                     ArtistsDao artistsDao,
                                                     ArtistsDaoWrapper artistsDaoWrapper,
                                                     AlbumsDao albumsDao,
                                                     AlbumsDaoWrapper albumsDaoWrapper) {
        return new StorageCompositionsInserter(libraryDatabase,
                compositionsDao,
                compositionsDaoWrapper,
                foldersDao,
                artistsDao,
                artistsDaoWrapper,
                albumsDao,
                albumsDaoWrapper);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageSourceRepository storageSourceRepository(CompositionsDaoWrapper compositionsDao,
                                                    StorageMusicProvider storageMusicProvider,
                                                    CompositionSourceEditor compositionSourceEditor,
                                                    @Named(IO_SCHEDULER) Scheduler ioScheduler) {
        return new StorageSourceRepositoryImpl(compositionsDao, storageMusicProvider, compositionSourceEditor, ioScheduler);
    }

}
