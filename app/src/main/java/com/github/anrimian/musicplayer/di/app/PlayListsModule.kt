package com.github.anrimian.musicplayer.di.app

import android.content.Context
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistsDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.playlists.PlaylistsRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.PlaylistFilesStorage
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylistsProvider
import com.github.anrimian.musicplayer.di.mvvm.ViewModelAssistedFactory
import com.github.anrimian.musicplayer.di.utils.ViewModelKey
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.PlaylistsRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.ui.common.delete.FileDeletionHandler
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListPresenter
import com.github.anrimian.musicplayer.ui.playlists.create.CreatePlayListPresenter
import com.github.anrimian.musicplayer.ui.playlists.create.CreatePlaylistViewModel
import com.github.anrimian.musicplayer.ui.playlists.details.PlaylistDetailsViewModel
import com.github.anrimian.musicplayer.ui.playlists.list.PlaylistsViewModel
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlaylistViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named
import javax.inject.Singleton

@Module
class PlayListsModule {

    @Provides
    @IntoMap
    @ViewModelKey(PlaylistsViewModel::class)
    fun playListsViewModel(
        playListsInteractor: PlaylistsInteractor,
        playerInteractor: LibraryPlayerInteractor,
        errorParser: ErrorParser,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        PlaylistsViewModel(
            playListsInteractor,
            playerInteractor,
            handle,
            errorParser
        )
    }

    @Provides
    @IntoMap
    @ViewModelKey(PlaylistDetailsViewModel::class)
    fun playListViewModel(
        musicPlayerInteractor: LibraryPlayerInteractor,
        playListsInteractor: PlaylistsInteractor,
        displaySettingsInteractor: DisplaySettingsInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        fileDeletionHandler: FileDeletionHandler,
        errorParser: ErrorParser
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        PlaylistDetailsViewModel(
            musicPlayerInteractor,
            playListsInteractor,
            displaySettingsInteractor,
            syncInteractor,
            fileDeletionHandler,
            handle,
            errorParser,
        )
    }

    @Provides
    @IntoMap
    @ViewModelKey(RenamePlaylistViewModel::class)
    fun renamePlaylistViewModel(
        playListsInteractor: PlaylistsInteractor,
        errorParser: ErrorParser,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        RenamePlaylistViewModel(
            playListsInteractor,
            handle,
            errorParser
        )
    }

    @Provides
    @IntoMap
    @ViewModelKey(CreatePlaylistViewModel::class)
    fun createPlaylistViewModel(
        playListsInteractor: PlaylistsInteractor,
        errorParser: ErrorParser,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        CreatePlaylistViewModel(
            playListsInteractor,
            handle,
            errorParser
        )
    }

    @Provides
    fun choosePlayListPresenter(
        playListsInteractor: PlaylistsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser,
    ) = ChoosePlayListPresenter(playListsInteractor, uiSchedule, errorParser)

    @Provides
    fun createPlayListPresenter(
        playListsInteractor: PlaylistsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser,
    ) = CreatePlayListPresenter(playListsInteractor, uiSchedule, errorParser)

    @Provides
    fun playListsInteractor(
        playerInteractor: LibraryPlayerInteractor,
        playListsRepository: PlaylistsRepository,
        settingsRepository: SettingsRepository,
        uiStateRepository: UiStateRepository,
        analytics: Analytics,
    ) = PlaylistsInteractor(
        playerInteractor,
        playListsRepository,
        settingsRepository,
        uiStateRepository,
        analytics
    )

    @Provides
    @Singleton
    fun storagePlayListDataSource(
        context: Context,
        settingsRepository: SettingsRepository,
        playListsProvider: StoragePlaylistsProvider,
        compositionsDaoWrapper: CompositionsDaoWrapper,
        playListsDaoWrapper: PlaylistsDaoWrapper,
        playlistFilesStorage: PlaylistFilesStorage,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler,
        @Named(AppSchedulerModule.DB_SCHEDULER) dbScheduler: Scheduler,
        @Named(AppSchedulerModule.SLOW_BG_SCHEDULER) slowBgScheduler: Scheduler,
    ): PlaylistsRepository = PlaylistsRepositoryImpl(
        context,
        settingsRepository,
        playListsProvider,
        compositionsDaoWrapper,
        playListsDaoWrapper,
        playlistFilesStorage,
        ioScheduler,
        dbScheduler,
        slowBgScheduler
    )

    @Provides
    fun storagePlayListsProvider(
        context: Context
    ) = StoragePlaylistsProvider(context)
}
