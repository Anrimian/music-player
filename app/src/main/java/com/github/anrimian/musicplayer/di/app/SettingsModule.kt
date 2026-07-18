package com.github.anrimian.musicplayer.di.app

import android.content.Context
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.data.repositories.settings.SettingsRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl
import com.github.anrimian.musicplayer.di.mvvm.ViewModelAssistedFactory
import com.github.anrimian.musicplayer.di.utils.ViewModelKey
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController
import com.github.anrimian.musicplayer.domain.interactors.library.MissingFilesInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.HeadsetSettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.MenuConfigInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.storage.StorageScannerInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.MenuConfigViewModel
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsPresenter
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsViewModel
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsViewModel
import com.github.anrimian.musicplayer.ui.settings.main.SettingsViewModel
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsPresenter
import com.github.anrimian.musicplayer.ui.settings.player.impls.EnabledMediaPlayersPresenter
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named
import javax.inject.Singleton

@Module
class SettingsModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        context: Context
    ): SettingsRepository = SettingsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideUiStateRepository(
        context: Context
    ): UiStateRepository = UiStateRepositoryImpl(context)

    @Provides
    @Singleton
    fun uiStateRepository(
        context: Context
    ): StateRepository = StateRepositoryImpl(context)

    @Provides
    fun displaySettingsInteractor(
        settingsRepository: SettingsRepository
    ) = DisplaySettingsInteractor(settingsRepository)

    @Provides
    fun headsetSettingsInteractor(
        settingsRepository: SettingsRepository
    ) = HeadsetSettingsInteractor(settingsRepository)

    @Provides
    fun displaySettingsPresenter(
        displaySettingsInteractor: DisplaySettingsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = DisplaySettingsPresenter(displaySettingsInteractor, uiScheduler, errorParser)

    @Provides
    @IntoMap
    @ViewModelKey(HeadsetSettingsViewModel::class)
    fun headsetSettingsViewModel(
        interactor: HeadsetSettingsInteractor,
        errorParser: ErrorParser
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        HeadsetSettingsViewModel(interactor, handle, errorParser)
    }

    @Provides
    fun playerSettingsInteractor(
        settingsRepository: SettingsRepository,
        musicPlayerController: MusicPlayerController
    ) = PlayerSettingsInteractor(settingsRepository, musicPlayerController)

    @Provides
    fun playerSettingsPresenter(
        playerSettingsInteractor: PlayerSettingsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = PlayerSettingsPresenter(playerSettingsInteractor, uiScheduler, errorParser)

    @Provides
    @IntoMap
    @ViewModelKey(LibrarySettingsViewModel::class)
    fun librarySettingsViewModel(
        librarySettingsInteractor: LibrarySettingsInteractor,
        errorParser: ErrorParser
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        LibrarySettingsViewModel(librarySettingsInteractor, handle, errorParser)
    }

    @Provides
    fun librarySettingsInteractor(
        settingsRepository: SettingsRepository,
        libraryRepository: LibraryRepository,
        storageScannerInteractor: StorageScannerInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>
    ) = LibrarySettingsInteractor(
        settingsRepository,
        libraryRepository,
        storageScannerInteractor,
        syncInteractor
    )

    @Provides
    fun enabledMediaPlayersPresenter(
        playerSettingsInteractor: PlayerSettingsInteractor
    ) = EnabledMediaPlayersPresenter(playerSettingsInteractor)

    @Provides
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun settingsViewModel(
        missingFilesInteractor: MissingFilesInteractor,
        storageScannerInteractor: StorageScannerInteractor,
        errorParser: ErrorParser
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        SettingsViewModel(
            missingFilesInteractor,
            storageScannerInteractor,
            handle,
            errorParser
        )
    }

    @Provides
    fun menuConfigInteractor(
        settingsRepository: SettingsRepository
    ) = MenuConfigInteractor(settingsRepository)

    @Provides
    @IntoMap
    @ViewModelKey(MenuConfigViewModel::class)
    fun menuConfigViewModel(
        menuConfigInteractor: MenuConfigInteractor,
        errorParser: ErrorParser
    ): ViewModelAssistedFactory<*> = ViewModelAssistedFactory { handle ->
        MenuConfigViewModel(menuConfigInteractor, handle, errorParser)
    }
}

