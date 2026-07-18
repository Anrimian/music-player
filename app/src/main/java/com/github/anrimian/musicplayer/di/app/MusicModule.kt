package com.github.anrimian.musicplayer.di.app

import android.content.Context
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.data.controllers.music.MusicPlayerControllerImpl
import com.github.anrimian.musicplayer.data.controllers.music.SystemMusicControllerImpl
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.external.ExternalEqualizer
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository
import com.github.anrimian.musicplayer.data.repositories.library.LibraryRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.play_queue.PlayQueueRepositoryImpl
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryCompositionsInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.CommonPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerCoordinatorInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository
import com.github.anrimian.musicplayer.domain.repositories.ExternalMediaSourceRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryFilesRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageScannerRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.infrastructure.MediaSessionHandler
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named
import javax.inject.Singleton

@Module
class MusicModule {

    @Provides
    @Singleton
    fun playerInteractor(
        musicPlayerController: MusicPlayerController,
        compositionSourceInteractor: CompositionSourceInteractor,
        playerErrorParser: PlayerErrorParser,
        systemMusicController: SystemMusicController,
        systemServiceController: SystemServiceController,
        settingsRepository: SettingsRepository,
        @Named(AppSchedulerModule.COMPUTATION_SCHEDULER) delayedEventsScheduler: Scheduler,
        analytics: Analytics
    ) = PlayerInteractor(
        musicPlayerController,
        compositionSourceInteractor,
        playerErrorParser,
        systemMusicController,
        systemServiceController,
        settingsRepository,
        delayedEventsScheduler,
        analytics,
        maxRePrepareTries = 2
    )

    @Provides
    @Singleton
    fun compositionSourceInteractor(
        storageSourceRepository: StorageSourceRepository,
        syncInteractor: SyncInteractor<FileKey, *, Long>
    ) = CompositionSourceInteractor(storageSourceRepository, syncInteractor)

    @Provides
    @Singleton
    fun playerCoordinatorInteractor(
        playerInteractor: PlayerInteractor,
        uiStateRepository: UiStateRepository
    ) = PlayerCoordinatorInteractor(playerInteractor, uiStateRepository)

    @Provides
    @Singleton
    fun externalPlayerInteractor(
        interactor: PlayerCoordinatorInteractor,
        externalMediaSourceRepository: ExternalMediaSourceRepository,
        settingsRepository: SettingsRepository,
        systemMusicController: SystemMusicController
    ) = ExternalPlayerInteractor(
        interactor,
        externalMediaSourceRepository,
        settingsRepository,
        systemMusicController
    )

    @Provides
    @Singleton
    fun libraryPlayerInteractor(
        playerCoordinatorInteractor: PlayerCoordinatorInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        settingsRepository: SettingsRepository,
        playQueueRepository: PlayQueueRepository,
        musicProviderRepository: LibraryRepository,
        uiStateRepository: UiStateRepository,
        analytics: Analytics
    ) = LibraryPlayerInteractor(
        playerCoordinatorInteractor,
        syncInteractor,
        settingsRepository,
        playQueueRepository,
        musicProviderRepository,
        uiStateRepository,
        analytics
    )

    @Provides
    @Singleton
    fun playQueueRepository(
        playQueueDao: PlayQueueDaoWrapper,
        settingsPreferences: SettingsRepository,
        uiStateRepository: UiStateRepository,
        @Named(AppSchedulerModule.DB_SCHEDULER) dbScheduler: Scheduler
    ): PlayQueueRepository = PlayQueueRepositoryImpl(
        playQueueDao,
        settingsPreferences,
        uiStateRepository,
        dbScheduler
    )

    @Provides
    @Singleton
    fun provideSystemMusicController(
        context: Context
    ): SystemMusicController = SystemMusicControllerImpl(context)

    @Provides
    @Singleton
    fun musicPlayerController(
        settingsRepository: SettingsRepository,
        context: Context,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        equalizerController: EqualizerController,
        exoPlayerMediaItemBuilder: ExoPlayerMediaItemBuilder,
        mediaPlayerSourceBuilder: MediaPlayerDataSourceBuilder,
        analytics: Analytics
    ): MusicPlayerController = MusicPlayerControllerImpl(
        settingsRepository,
        context,
        ioScheduler,
        uiScheduler,
        equalizerController,
        exoPlayerMediaItemBuilder,
        mediaPlayerSourceBuilder,
        analytics
    )

    @Provides
    @Singleton
    fun musicProviderRepository(
        storageFilesDataSource: StorageFilesDataSource,
        compositionsDao: CompositionsDaoWrapper,
        artistsDao: ArtistsDaoWrapper,
        albumsDao: AlbumsDaoWrapper,
        genresDao: GenresDaoWrapper,
        foldersDao: FoldersDaoWrapper,
        ignoredFoldersDao: IgnoredFoldersDao,
        settingsPreferences: SettingsRepository,
        storageScannerRepository: StorageScannerRepository,
        @Named(SchedulerModule.IO_SCHEDULER) scheduler: Scheduler
    ): LibraryRepository = LibraryRepositoryImpl(
        storageFilesDataSource,
        compositionsDao,
        artistsDao,
        albumsDao,
        genresDao,
        foldersDao,
        ignoredFoldersDao,
        settingsPreferences,
        storageScannerRepository,
        scheduler
    )

    @Provides
    @Singleton
    fun commonPlayerInteractor(
        playerCoordinatorInteractor: PlayerCoordinatorInteractor,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        externalPlayerInteractor: ExternalPlayerInteractor,
        playerInteractor: PlayerInteractor
    ) = CommonPlayerInteractor(
        playerCoordinatorInteractor,
        libraryPlayerInteractor,
        externalPlayerInteractor,
        playerInteractor
    )

    @Provides
    @Singleton
    fun musicServiceInteractor(
        commonPlayerInteractor: CommonPlayerInteractor,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        libraryCompositionsInteractor: LibraryCompositionsInteractor,
        libraryFoldersInteractor: LibraryFoldersInteractor,
        libraryArtistsInteractor: LibraryArtistsInteractor,
        libraryAlbumsInteractor: LibraryAlbumsInteractor,
        libraryGenresInteractor: LibraryGenresInteractor,
        playListsInteractor: PlaylistsInteractor,
        settingsRepository: SettingsRepository
    ) = MusicServiceInteractor(
        commonPlayerInteractor,
        libraryPlayerInteractor,
        libraryCompositionsInteractor,
        libraryFoldersInteractor,
        libraryArtistsInteractor,
        libraryAlbumsInteractor,
        libraryGenresInteractor,
        playListsInteractor,
        settingsRepository
    )

    @Provides
    @Singleton
    fun coverImageLoader(
        context: Context,
        themeController: ThemeController
    ) = CoverImageLoader(context, themeController)

    @Provides
    @Singleton
    fun equalizerController(
        settingsRepository: SettingsRepository,
        externalEqualizer: ExternalEqualizer,
        internalEqualizer: InternalEqualizer
    ) = EqualizerController(settingsRepository, externalEqualizer, internalEqualizer)

    @Provides
    @Singleton
    fun externalEqualizer(
        context: Context
    ) = ExternalEqualizer(context)

    @Provides
    @Singleton
    fun internalEqualizer(
        equalizerStateRepository: EqualizerStateRepository,
        analytics: Analytics
    ) = InternalEqualizer(equalizerStateRepository, analytics)

    @Provides
    @Singleton
    fun equalizerStateRepository(
        context: Context
    ) = EqualizerStateRepository(context)

    @Provides
    fun equalizerPresenter(
        equalizerInteractor: EqualizerInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) scheduler: Scheduler,
        errorParser: ErrorParser
    ) = EqualizerPresenter(equalizerInteractor, scheduler, errorParser)

    @Provides
    fun equalizerInteractor(
        equalizerRepository: EqualizerRepository
    ) = EqualizerInteractor(equalizerRepository)

    @Provides
    fun equalizerRepository(
        internalEqualizer: InternalEqualizer
    ): EqualizerRepository = EqualizerRepositoryImpl(internalEqualizer)

    @Provides
    @Singleton
    fun mediaSessionHandler(
        context: Context,
        playerInteractor: PlayerInteractor,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        musicServiceInteractor: MusicServiceInteractor,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = MediaSessionHandler(
        context,
        playerInteractor,
        libraryPlayerInteractor,
        musicServiceInteractor,
        ioScheduler,
        uiScheduler,
        errorParser
    )

    @Provides
    fun libraryCompositionsInteractor(
        musicProviderRepository: LibraryRepository,
        settingsRepository: SettingsRepository,
        uiStateRepository: UiStateRepository
    ) = LibraryCompositionsInteractor(
        musicProviderRepository,
        settingsRepository,
        uiStateRepository
    )

    @Provides
    fun libraryFilesInteractor(
        musicProviderRepository: LibraryRepository,
        libraryFilesRepository: LibraryFilesRepository,
        musicPlayerInteractor: LibraryPlayerInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        settingsRepository: SettingsRepository,
        uiStateRepository: UiStateRepository
    ) = LibraryFoldersInteractor(
        musicProviderRepository,
        libraryFilesRepository,
        musicPlayerInteractor,
        syncInteractor,
        settingsRepository,
        uiStateRepository
    )

    @Provides
    fun libraryArtistsInteractor(
        repository: LibraryRepository,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        settingsRepository: SettingsRepository,
        uiStateRepository: UiStateRepository
    ) = LibraryArtistsInteractor(
        repository,
        libraryPlayerInteractor,
        settingsRepository,
        uiStateRepository
    )

    @Provides
    fun libraryAlbumsInteractor(
        repository: LibraryRepository,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        settingsRepository: SettingsRepository,
        uiStateRepository: UiStateRepository
    ) = LibraryAlbumsInteractor(
        repository,
        libraryPlayerInteractor,
        settingsRepository,
        uiStateRepository
    )

    @Provides
    fun libraryGenresInteractor(
        playerInteractor: LibraryPlayerInteractor,
        repository: LibraryRepository,
        settingsRepository: SettingsRepository,
        uiStateRepository: UiStateRepository
    ) = LibraryGenresInteractor(
        playerInteractor,
        repository,
        settingsRepository,
        uiStateRepository
    )
}
