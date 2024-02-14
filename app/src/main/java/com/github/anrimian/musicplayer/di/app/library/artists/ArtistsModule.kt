package com.github.anrimian.musicplayer.di.app.library.artists

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class ArtistsModule {

    @Provides
    fun artistsListPresenter(
        interactor: LibraryArtistsInteractor,
        playerInteractor: LibraryPlayerInteractor,
        playListsInteractor: PlayListsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = ArtistsListPresenter(
        interactor,
        playerInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )

}