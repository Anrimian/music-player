package com.github.anrimian.musicplayer.di.app.play_list

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlayListPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class PlayListModule(private val playListId: Long) {

    @Provides
    fun changePlayListPresenter(
        playListsInteractor: PlaylistsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiSchedule: Scheduler,
        errorParser: ErrorParser
    ) = RenamePlayListPresenter(
        playListId,
        playListsInteractor,
        uiSchedule,
        errorParser
    )

}
