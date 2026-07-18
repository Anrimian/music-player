package com.github.anrimian.musicplayer.di.app

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.di.mvvm.ViewModelAssistedFactory
import com.github.anrimian.musicplayer.di.utils.ViewModelKey
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.screen.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.common.DeviceCapabilities
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.compositions.DeleteCompositionsViewModel
import com.github.anrimian.musicplayer.ui.common.delete.FileDeletionHandler
import com.github.anrimian.musicplayer.ui.common.dialogs.share.ShareViewModel
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderViewModel
import com.github.anrimian.musicplayer.ui.player_screen.PlayerPresenter
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.LyricsViewModel
import com.github.anrimian.musicplayer.ui.player_screen.queue.PlayQueuePresenter
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class LibraryModule {

    @Provides
    fun playerPresenter(
        musicPlayerInteractor: LibraryPlayerInteractor,
        playerScreenInteractor: PlayerScreenInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playListsInteractor: PlaylistsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
    ) = PlayerPresenter(
        musicPlayerInteractor,
        playerScreenInteractor,
        syncInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )

    @Provides
    fun playQueuePresenter(
        musicPlayerInteractor: LibraryPlayerInteractor,
        playerScreenInteractor: PlayerScreenInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playListsInteractor: PlaylistsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
    ) = PlayQueuePresenter(
        musicPlayerInteractor,
        playerScreenInteractor,
        syncInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )

    @Provides
    @IntoMap
    @ViewModelKey(LyricsViewModel::class)
    fun lyricsViewModel(
        libraryPlayerInteractor: LibraryPlayerInteractor,
        errorParser: ErrorParser,
        deviceCapabilities: DeviceCapabilities,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        LyricsViewModel(
            libraryPlayerInteractor,
            handle,
            errorParser,
            deviceCapabilities
        )
    }

    @Provides
    fun fileDeletionHandler(
        playerInteractor: LibraryPlayerInteractor,
        storageFilesDataSource: StorageFilesDataSource,
        settingsInteractor: LibrarySettingsInteractor,
        deviceCapabilities: DeviceCapabilities
    ) = FileDeletionHandler(
        playerInteractor,
        storageFilesDataSource,
        settingsInteractor,
        deviceCapabilities
    )

    @Provides
    @IntoMap
    @ViewModelKey(ExcludedFoldersViewModel::class)
    fun excludedFoldersViewModel(
        interactor: LibraryFoldersInteractor,
        errorParser: ErrorParser,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        ExcludedFoldersViewModel(
            interactor,
            handle,
            errorParser
        )
    }

    @Provides
    @IntoMap
    @ViewModelKey(ShareViewModel::class)
    fun shareViewModel(
        sourceInteractor: CompositionSourceInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        errorParser: ErrorParser,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        ShareViewModel(
            sourceInteractor,
            syncInteractor,
            handle,
            errorParser
        )
    }

    @Provides
    @IntoMap
    @ViewModelKey(DeleteCompositionsViewModel::class)
    fun deleteCompositionsViewModel(
        librarySettingsInteractor: LibrarySettingsInteractor,
        errorParser: ErrorParser,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        DeleteCompositionsViewModel(
            librarySettingsInteractor,
            handle,
            errorParser
        )
    }

    @Provides
    @IntoMap
    @ViewModelKey(SelectOrderViewModel::class)
    fun selectOrderViewModel(
        displaySettingsInteractor: DisplaySettingsInteractor,
        errorParser: ErrorParser,
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        SelectOrderViewModel(
            displaySettingsInteractor,
            handle,
            errorParser
        )
    }
}
