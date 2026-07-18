package com.github.anrimian.musicplayer.ui.settings.folders

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel


class ExcludedFoldersViewModel(
    private val interactor: LibraryFoldersInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : SimpleViewModel<ExcludedFoldersState>(
    ExcludedFoldersState(),
    savedStateHandle,
    errorParser
) {

    private var recentlyRemovedFolder: IgnoredFolder? = null

    init {
        interactor.getIgnoredFoldersFlow().subscribeStatedList(R.string.no_excluded_folders) { state ->
            updateState { copy(folders = state) }
        }
    }

    fun onDeleteFolderClicked(folder: IgnoredFolder) {
        launch(onError = ::sendErrorMessage) {
            interactor.deleteIgnoredFolder(folder)
            onFolderRemoved(folder)
        }
    }

    fun onRestoreRemovedFolderClicked() {
        if (recentlyRemovedFolder == null) {
            return
        }
        launch(onError = ::sendErrorMessage) {
            interactor.addFolderToIgnore(recentlyRemovedFolder!!)
        }
    }

    private fun onFolderRemoved(folder: IgnoredFolder) {
        recentlyRemovedFolder = folder
        sendMessage(
            messageId = R.string.ignored_folder_removed,
            actionLabelId = R.string.cancel,
            action = RestoreRemovedFolder,
        )
    }

}