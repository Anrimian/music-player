package com.github.anrimian.musicplayer.di.app.library.albums

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class AlbumsModule {

    @Provides
    fun artistsListPresenter(
        interactor: LibraryAlbumsInteractor,
        playerInteractor: LibraryPlayerInteractor,
        playListsInteractor: PlayListsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = AlbumsListPresenter(
        interactor,
        playerInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )

}