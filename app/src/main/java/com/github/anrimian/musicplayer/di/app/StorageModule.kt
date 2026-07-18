package com.github.anrimian.musicplayer.di.app

import android.content.Context
import android.os.Build
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistsDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.library.LibraryFilesRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.library.edit.EditorRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.player.ExternalAudioFileCache
import com.github.anrimian.musicplayer.data.repositories.player.ExternalMediaSourceRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.scanner.FileFilter
import com.github.anrimian.musicplayer.data.repositories.scanner.StorageAudioFileAnalyzer
import com.github.anrimian.musicplayer.data.repositories.scanner.StorageScannerRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.scanner.files.FileScanner
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.PlaylistFilesStorage
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.StoragePlaylistsAnalyzer
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSourceApi30
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSourceImpl
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylistsProvider
import com.github.anrimian.musicplayer.data.storage.providers.volumes.VolumeProvider
import com.github.anrimian.musicplayer.data.storage.providers.volumes.VolumeProviderImpl
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.data.storage.source.ContentSourceHelper
import com.github.anrimian.musicplayer.data.storage.source.FileSourceProvider
import com.github.anrimian.musicplayer.data.storage.source.StorageSourceRepositoryImpl
import com.github.anrimian.musicplayer.di.config.AppSetupConfig
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.domain.interactors.storage.StorageScannerInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository
import com.github.anrimian.musicplayer.domain.repositories.ExternalMediaSourceRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryFilesRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository
import com.github.anrimian.musicplayer.domain.repositories.PlaylistsRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageScannerRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named
import javax.inject.Singleton

@Module
class StorageModule {

    @Provides
    @Singleton
    fun storageMusicProvider(
        context: Context,
        analytics: Analytics,
    ) = SystemAudioCatalogProvider(
        context,
        analytics,
    )

    @Provides
    @Singleton
    fun storageGenresProvider(context: Context) = StorageGenresProvider(context)

    @Provides
    @Singleton
    fun storageAlbumsProvider(context: Context) = StorageAlbumsProvider(context)

    @Provides
    @Singleton
    fun fileSourceProvider(context: Context) = FileSourceProvider(context)

    @Provides
    @Singleton
    fun compositionSourceEditor(
        musicProvider: SystemAudioCatalogProvider,
        fileSourceProvider: FileSourceProvider,
        contentSourceHelper: ContentSourceHelper
    ) = CompositionSourceEditor(musicProvider, fileSourceProvider, contentSourceHelper)

    @Provides
    @Singleton
    fun compositionEditorRepository(
        sourceEditor: CompositionSourceEditor,
        compositionsDao: CompositionsDaoWrapper,
        albumsDao: AlbumsDaoWrapper,
        artistsDao: ArtistsDaoWrapper,
        genresDao: GenresDaoWrapper,
        storageMusicProvider: SystemAudioCatalogProvider,
        @Named(AppSchedulerModule.DB_SCHEDULER) scheduler: Scheduler
    ): EditorRepository = EditorRepositoryImpl(
        sourceEditor,
        compositionsDao,
        albumsDao,
        artistsDao,
        genresDao,
        storageMusicProvider,
        scheduler
    )

    @Provides
    @Singleton
    fun libraryFilesRepository(
        filesDataSource: StorageFilesDataSource,
        libraryDatabase: LibraryDatabase,
        compositionsDao: CompositionsDaoWrapper,
        foldersDao: FoldersDaoWrapper,
        playListsDao: PlaylistsDaoWrapper,
        storageMusicProvider: SystemAudioCatalogProvider,
        playListsRepository: PlaylistsRepository,
        libraryRepository: LibraryRepository,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler,
        appSetupConfig: AppSetupConfig
    ): LibraryFilesRepository = LibraryFilesRepositoryImpl(
        filesDataSource,
        libraryDatabase,
        compositionsDao,
        foldersDao,
        playListsDao,
        storageMusicProvider,
        playListsRepository,
        libraryRepository,
        ioScheduler,
        appSetupConfig.isPathChangeForNonExistentFilesAllowed
    )

    @Provides
    @Singleton
    fun storageFilesDataSource(musicProvider: SystemAudioCatalogProvider): StorageFilesDataSource {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            StorageFilesDataSourceApi30(musicProvider)
        } else {
            StorageFilesDataSourceImpl(musicProvider)
        }
    }

    @Provides
    fun compositionEditorInteractor(
        sourceInteractor: CompositionSourceInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        editorRepository: EditorRepository,
        libraryRepository: LibraryRepository,
        libraryFilesRepository: LibraryFilesRepository,
        storageSourceRepository: StorageSourceRepository
    ) = EditorInteractor(
        sourceInteractor,
        syncInteractor,
        editorRepository,
        libraryRepository,
        libraryFilesRepository,
        storageSourceRepository
    )

    @Provides
    @Singleton
    fun fileScanner(
        compositionsDao: CompositionsDaoWrapper,
        compositionSourceEditor: CompositionSourceEditor,
        stateRepository: StateRepository,
        storageSourceRepository: StorageSourceRepository,
        analytics: Analytics,
        @Named(AppSchedulerModule.SLOW_BG_SCHEDULER) scheduler: Scheduler
    ) = FileScanner(
        compositionsDao,
        compositionSourceEditor,
        stateRepository,
        storageSourceRepository,
        analytics,
        scheduler
    )

    @Provides
    @Singleton
    fun storageScannerRepository(
        musicProvider: SystemAudioCatalogProvider,
        playListsProvider: StoragePlaylistsProvider,
        compositionsDao: CompositionsDaoWrapper,
        stateRepository: StateRepository,
        settingsRepository: SettingsRepository,
        compositionAnalyzer: StorageAudioFileAnalyzer,
        storagePlaylistAnalyzer: StoragePlaylistsAnalyzer,
        fileScanner: FileScanner,
        loggerRepository: LoggerRepository,
        analytics: Analytics,
        @Named(SchedulerModule.IO_SCHEDULER) scheduler: Scheduler
    ): StorageScannerRepository = StorageScannerRepositoryImpl(
        musicProvider,
        playListsProvider,
        compositionsDao,
        stateRepository,
        settingsRepository,
        compositionAnalyzer,
        storagePlaylistAnalyzer,
        fileScanner,
        loggerRepository,
        analytics,
        scheduler
    )

    @Provides
    @Singleton
    fun storageScannerInteractor(
        storageScannerRepository: StorageScannerRepository,
        syncInteractor: SyncInteractor<FileKey, *, Long>
    ) = StorageScannerInteractor(storageScannerRepository, syncInteractor)

    @Provides
    @Singleton
    fun fileFilter(settingsRepository: SettingsRepository) = FileFilter(settingsRepository)

    @Provides
    fun storageAudioFileAnalyzer(
        compositionsDao: CompositionsDaoWrapper,
        ignoredFoldersDao: IgnoredFoldersDao,
        settingsRepository: SettingsRepository,
        compositionsInserter: StorageCompositionsInserter,
        fileFilter: FileFilter
    ) = StorageAudioFileAnalyzer(
        compositionsDao,
        ignoredFoldersDao,
        settingsRepository,
        compositionsInserter,
        fileFilter
    )

    @Provides
    fun storagePlaylistsAnalyzer(
        compositionsDao: CompositionsDaoWrapper,
        playListsDao: PlaylistsDaoWrapper,
        playListsProvider: StoragePlaylistsProvider,
        playlistFilesStorage: PlaylistFilesStorage
    ) = StoragePlaylistsAnalyzer(
        compositionsDao,
        playListsDao,
        playListsProvider,
        playlistFilesStorage
    )

    @Provides
    fun playlistFilesStorage(context: Context, analytics: Analytics) =
        PlaylistFilesStorage(context, analytics)

    @Provides
    fun compositionsInserter(
        libraryDatabase: LibraryDatabase,
        compositionsDao: CompositionsDao,
        compositionsDaoWrapper: CompositionsDaoWrapper,
        foldersDao: FoldersDaoWrapper,
        artistsDao: ArtistsDao,
        artistsDaoWrapper: ArtistsDaoWrapper,
        albumsDao: AlbumsDao,
        genreDao: GenreDao
    ) = StorageCompositionsInserter(
        libraryDatabase,
        compositionsDao,
        compositionsDaoWrapper,
        foldersDao,
        artistsDao,
        artistsDaoWrapper,
        albumsDao,
        genreDao
    )

    @Provides
    @Singleton
    fun storageSourceRepository(
        compositionsDao: CompositionsDaoWrapper,
        storageMusicProvider: SystemAudioCatalogProvider,
        compositionSourceEditor: CompositionSourceEditor,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler
    ): StorageSourceRepository = StorageSourceRepositoryImpl(
        compositionsDao,
        storageMusicProvider,
        compositionSourceEditor,
        ioScheduler
    )

    @Provides
    @Singleton
    fun externalAudioFileCache(
        context: Context,
        analytics: Analytics,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler
    ) = ExternalAudioFileCache(context, analytics, ioScheduler)

    @Provides
    @Singleton
    fun externalMediaSourceRepository(
        context: Context,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler,
        cache: ExternalAudioFileCache
    ): ExternalMediaSourceRepository = ExternalMediaSourceRepositoryImpl(context, ioScheduler, cache)

    @Provides
    @Singleton
    fun volumeProvider(context: Context): VolumeProvider = VolumeProviderImpl(context)
}
