package com.github.anrimian.musicplayer.di.app.library.files

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.folders.root.FolderRootPresenter
import com.github.anrimian.musicplayer.ui.library.folders.volumes.LibraryVolumesPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class LibraryFilesModule {
    
    @Provides
    fun folderRootPresenter(
        interactor: LibraryFoldersScreenInteractor,
        errorParser: ErrorParser,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler
    ) = FolderRootPresenter(
        interactor,
        errorParser,
        uiScheduler
    )

    @Provides
    fun libraryVolumesPresenter(
        interactor: LibraryFoldersScreenInteractor,
        playerInteractor: LibraryPlayerInteractor,
        playListsInteractor: PlaylistsInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser,
    ) = LibraryVolumesPresenter(
        interactor,
        playerInteractor,
        playListsInteractor,
        uiScheduler,
        errorParser
    )

    @Provides
    @LibraryFilesScope
    fun libraryFilesInteractor(
        foldersInteractor: LibraryFoldersInteractor,
        uiStateRepository: UiStateRepository
    ) = LibraryFoldersScreenInteractor(
        foldersInteractor,
        uiStateRepository
    )
    
}
