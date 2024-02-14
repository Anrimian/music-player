package com.github.anrimian.musicplayer.di.app.library.albums.items

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class AlbumItemsModule(private val id: Long) {

    @Provides
    fun genreItemsPresenter(
        interactor: LibraryAlbumsInteractor,
        playerInteractor: LibraryPlayerInteractor,
        displaySettingsInteractor: DisplaySettingsInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playListsInteractor: PlayListsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = AlbumItemsPresenter(
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