package com.github.anrimian.musicplayer.di.app.library.files.folder

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

/**
 * Created on 31.10.2017.
 */
@Module
class FolderModule(private val folderId: Long?) {
    
    @Provides
    fun libraryFoldersPresenter(
        interactor: LibraryFoldersScreenInteractor,
        playerInteractor: LibraryPlayerInteractor,
        displaySettingsInteractor: DisplaySettingsInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playListsInteractor: PlayListsInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = LibraryFoldersPresenter(
        folderId,
        interactor,
        playerInteractor,
        displaySettingsInteractor,
        syncInteractor,
        playListsInteractor,
        errorParser,
        uiScheduler
    )
    
}
