package com.github.anrimian.musicplayer.ui.settings.folders

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler


class ExcludedFoldersPresenter(private val interactor: LibraryFoldersInteractor,
                               uiScheduler: Scheduler,
                               errorParser: ErrorParser)
    : AppPresenter<ExcludedFoldersView>(uiScheduler, errorParser) {

    private var recentlyRemovedFolder: IgnoredFolder? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnIgnoredFoldersList()
    }

    fun onDeleteFolderClicked(folder: IgnoredFolder) {
        interactor.deleteIgnoredFolder(folder)
                .toSingleDefault(folder)
                .subscribeOnUi(this::onFolderRemoved, this::onDefaultError)
    }

    fun onRestoreRemovedFolderClicked() {
        if (recentlyRemovedFolder == null) {
            return
        }
        interactor.addFolderToIgnore(recentlyRemovedFolder)
                .justSubscribe(this::onFoldersListError)
    }

    private fun onFolderRemoved(folder: IgnoredFolder) {
        recentlyRemovedFolder = folder
        viewState.showRemovedFolderMessage(folder)
    }

    private fun subscribeOnIgnoredFoldersList() {
        interactor.ignoredFoldersObservable
                .subscribeOnUi(this::onFoldersListReceived, this::onFoldersListError)
    }

    private fun onFoldersListReceived(folders: List<IgnoredFolder>) {
        viewState.showExcludedFoldersList(folders)
        if (folders.isEmpty()) {
            viewState.showEmptyListState()
        } else {
            viewState.showListState()
        }
    }

    private fun onFoldersListError(throwable: Throwable) {
        viewState.showErrorState(errorParser.parseError(throwable))
    }

    private fun onDefaultError(throwable: Throwable) {
        viewState.showErrorMessage(errorParser.parseError(throwable))
    }

}