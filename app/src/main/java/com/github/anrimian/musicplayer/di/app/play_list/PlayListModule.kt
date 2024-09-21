package com.github.anrimian.musicplayer.di.app.play_list

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListPresenter
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class PlayListModule(private val playListId: Long) {
    
    @Provides
    fun playListsPresenter(
        musicPlayerInteractor: LibraryPlayerInteractor,
        playListsInteractor: PlayListsInteractor,
        displaySettingsInteractor: DisplaySettingsInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser
    ) = PlayListPresenter(
        playListId,
        musicPlayerInteractor,
        playListsInteractor,
        displaySettingsInteractor,
        syncInteractor,
        errorParser,
        uiSchedule
    )

    @Provides
    fun changePlayListPresenter(
        playListsInteractor: PlayListsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser
    ) = RenamePlayListPresenter(
        playListId,
        playListsInteractor,
        uiSchedule,
        errorParser
    )

}
