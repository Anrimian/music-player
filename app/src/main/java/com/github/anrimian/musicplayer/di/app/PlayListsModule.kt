package com.github.anrimian.musicplayer.di.app

import android.content.Context
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.playlists.PlayListsRepositoryImpl
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.PlaylistFilesStorage
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListPresenter
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListPresenter
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named
import javax.inject.Singleton

@Module
class PlayListsModule {
    
    @Provides
    fun playListsPresenter(
        playListsInteractor: PlayListsInteractor,
        playerInteractor: LibraryPlayerInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser
    ) = PlayListsPresenter(
        playListsInteractor,
        playerInteractor,
        uiSchedule,
        errorParser
    )

    @Provides
    fun choosePlayListPresenter(
        playListsInteractor: PlayListsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser
    ) = ChoosePlayListPresenter(playListsInteractor, uiSchedule, errorParser)

    @Provides
    fun createPlayListPresenter(
        playListsInteractor: PlayListsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser
    ) = CreatePlayListPresenter(playListsInteractor, uiSchedule, errorParser)

    @Provides
    fun playListsInteractor(
        playerInteractor: LibraryPlayerInteractor,
        playListsRepository: PlayListsRepository,
        settingsRepository: SettingsRepository,
        uiStateRepository: UiStateRepository,
        analytics: Analytics
    ) = PlayListsInteractor(
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
        playListsProvider: StoragePlayListsProvider,
        compositionsDaoWrapper: CompositionsDaoWrapper,
        playListsDaoWrapper: PlayListsDaoWrapper,
        playlistFilesStorage: PlaylistFilesStorage,
        @Named(SchedulerModule.DB_SCHEDULER) dbScheduler: Scheduler,
        @Named(SchedulerModule.SLOW_BG_SCHEDULER) slowBgScheduler: Scheduler
    ): PlayListsRepository = PlayListsRepositoryImpl(
        context,
        settingsRepository,
        playListsProvider,
        compositionsDaoWrapper,
        playListsDaoWrapper,
        playlistFilesStorage,
        dbScheduler,
        slowBgScheduler
    )

    @Provides
    fun storagePlayListsProvider(context: Context) = StoragePlayListsProvider(context)

}
