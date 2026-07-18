package com.github.anrimian.musicplayer.di.app.library.genres.items

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.genres.items.GenreItemsPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class GenreItemsModule(private val id: Long) {
    
    @Provides
    fun genreItemsPresenter(
        interactor: LibraryGenresInteractor,
        playerInteractor: LibraryPlayerInteractor,
        displaySettingsInteractor: DisplaySettingsInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playListsInteractor: PlaylistsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = GenreItemsPresenter(
        id,
        interactor,
        playerInteractor,
        displaySettingsInteractor,
        syncInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )

}
