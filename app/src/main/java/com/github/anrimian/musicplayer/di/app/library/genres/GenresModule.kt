package com.github.anrimian.musicplayer.di.app.library.genres

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class GenresModule {
    
    @Provides
    fun genreListPresenter(
        interactor: LibraryGenresInteractor,
        playerInteractor: LibraryPlayerInteractor,
        playListsInteractor: PlayListsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = GenresListPresenter(
        interactor,
        playerInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )
    
}
