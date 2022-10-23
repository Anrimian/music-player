package com.github.anrimian.musicplayer.ui.library.folders.root

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class FolderRootPresenter(
    private val interactor: LibraryFoldersScreenInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : AppPresenter<FolderRootView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showIdle()
    }

    fun onCreateFolderTreeRequested() {
        viewState.showProgress()
        interactor.currentFolderScreens
            .subscribeOnUi(this::onScreensReceived, this::onScreensReceivingError)
    }

    fun onNavigateToCompositionRequested(compositionId: Long) {
        interactor.getParentFolders(compositionId)
            .subscribeOnUi(this::onScreensReceived, this::onScreensReceivingError)
    }

    private fun onScreensReceivingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showError(errorCommand)
    }

    private fun onScreensReceived(ids: List<Long>) {
        viewState.showIdle()
        viewState.showFolderScreens(ids)
    }

}