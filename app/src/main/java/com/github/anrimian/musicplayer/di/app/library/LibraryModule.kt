package com.github.anrimian.musicplayer.di.app.library

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.player_screen.PlayerPresenter
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.LyricsPresenter
import com.github.anrimian.musicplayer.ui.player_screen.queue.PlayQueuePresenter
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

/**
 * Created on 29.10.2017.
 */
@Module
class LibraryModule {
    
    @Provides
    fun playerPresenter(
        musicPlayerInteractor: LibraryPlayerInteractor,
        playerScreenInteractor: PlayerScreenInteractor,
        playListsInteractor: PlayListsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = PlayerPresenter(
        musicPlayerInteractor,
        playerScreenInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )

    @Provides
    fun playQueuePresenter(
        musicPlayerInteractor: LibraryPlayerInteractor,
        playerScreenInteractor: PlayerScreenInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playListsInteractor: PlayListsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = PlayQueuePresenter(
        musicPlayerInteractor,
        playerScreenInteractor,
        syncInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )

    @Provides
    fun lyricsPresenter(
        libraryPlayerInteractor: LibraryPlayerInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = LyricsPresenter(
        libraryPlayerInteractor,
        errorParser,
        uiScheduler
    )

    @Provides
    fun excludedFoldersPresenter(
        interactor: LibraryFoldersInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = ExcludedFoldersPresenter(interactor, uiScheduler, errorParser)

}
