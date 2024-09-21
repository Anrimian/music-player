package com.github.anrimian.musicplayer.ui.settings.folders

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler


class ExcludedFoldersPresenter(
    private val interactor: LibraryFoldersInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<ExcludedFoldersView>(uiScheduler, errorParser) {

    private var recentlyRemovedFolder: IgnoredFolder? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnIgnoredFoldersList()
    }

    fun onDeleteFolderClicked(folder: IgnoredFolder) {
        interactor.deleteIgnoredFolder(folder)
            .toSingleDefault(folder)
            .launchOnUi(this::onFolderRemoved, viewState::showErrorMessage)
    }

    fun onRestoreRemovedFolderClicked() {
        if (recentlyRemovedFolder == null) {
            return
        }
        interactor.addFolderToIgnore(recentlyRemovedFolder!!)
            .justRunOnUi(viewState::showErrorMessage)
    }

    private fun onFolderRemoved(folder: IgnoredFolder) {
        recentlyRemovedFolder = folder
        viewState.showRemovedFolderMessage(folder)
    }

    private fun subscribeOnIgnoredFoldersList() {
        interactor.getIgnoredFoldersObservable()
            .runOnUi(this::onFoldersListReceived, viewState::showErrorState)
    }

    private fun onFoldersListReceived(folders: List<IgnoredFolder>) {
        viewState.showExcludedFoldersList(folders)
        if (folders.isEmpty()) {
            viewState.showEmptyListState()
        } else {
            viewState.showListState()
        }
    }

}