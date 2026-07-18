package com.github.anrimian.musicplayer.ui.settings.library

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.BaseViewModel
import com.github.anrimian.musicplayer.ui.common.mvvm.EmptyPersistent
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsDialogs.ConfirmAllowedExtensionsChangeDialog
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsDialogs.ConfirmMinDurationChangeDialog

class LibrarySettingsViewModel(
    private val interactor: LibrarySettingsInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
): BaseViewModel<LibrarySettingsState, EmptyPersistent>(
    LibrarySettingsState(),
    EmptyPersistent,
    savedStateHandle,
    errorParser
) {

    init {
        interactor.getAppConfirmDeleteDialogEnabledFlow().subscribe { isEnabled ->
            updateState { copy(isDoNotShowAppConfirmDialogEnabled = !isEnabled) }
        }
        interactor.getAudioFileMinDurationMillisFlow().subscribe { millis ->
            updateState { copy(audioFileMinDurationMillis = millis) }
        }
        interactor.getAllowedFileExtensionsFlow().subscribe { extensions ->
            updateState { copy(allowedFileExtensions = extensions) }
        }
        interactor.getPlaylistDuplicateCheckEnabledFlow().subscribe { isEnabled ->
            updateState { copy(playlistDuplicateCheckEnabled = isEnabled) }
        }
        updateState { copy(playlistInsertStartEnabled = interactor.isPlaylistInsertStartEnabled()) }
    }

    fun onExcludedFoldersClicked() {
        navigateTo(Screen.ExcludedFolders)
    }

    fun onEditAllowedExtensionsClicked() {
        showDialog(LibrarySettingsDialogs.EditAllowedExtensionsDialog(currentState.allowedFileExtensions))
    }

    fun onAllowedExtensionsEditCompleted(extensions: Set<String>) {
        dismissDialog()
        launch(onError = ::sendErrorMessage) {
            val filesCount = interactor.checkAllowedFileExtensionsModify(extensions)
            onAllowedExtensionsModifyCheckFinished(filesCount, extensions)
        }
    }

    fun onEditAllowedExtensionsDialogClosed() {
        dismissDialog()
    }

    fun onAllowedExtensionsChangeConfirmed() = withCurrentDialog<ConfirmAllowedExtensionsChangeDialog> { dialogData ->
        dismissDialog()
        applyAllowedFileExtensionsChange(dialogData.newExtensions)
    }

    fun onDoNotAppConfirmDialogChecked(isChecked: Boolean) {
        interactor.setAppConfirmDeleteDialogEnabled(!isChecked)
    }

    fun onAudioFileMinDurationMillisPicked(millis: Long) {
        dismissDialog()
        launch(onError = ::sendErrorMessage) {
            val filesCount = interactor.checkMinDurationModify(millis)
            onMinDurationModifyCheckFinished(filesCount, millis)
        }
    }

    fun onMinDurationChangeConfirmed() = withCurrentDialog<ConfirmMinDurationChangeDialog> { dialogData ->
        dismissDialog()
        applyAudioFileMinDurationMillisChange(dialogData.newMillis)
    }

    fun onPlaylistInsertStartChecked(isChecked: Boolean) {
        interactor.setPlaylistInsertStartEnabled(isChecked)
        updateState { copy(playlistInsertStartEnabled = isChecked) }
    }

    fun onPlaylistDuplicateCheckChecked(isChecked: Boolean) {
        interactor.setPlaylistDuplicateCheckEnabled(isChecked)
    }

    fun onSelectMinDurationClicked() {
        showDialog(LibrarySettingsDialogs.SelectMinDurationDialog(currentState.audioFileMinDurationMillis))
    }

    fun onSelectMinDurationDialogClosed() {
        dismissDialog()
    }

    fun onConfirmDeleteDialogClosed() {
        dismissDialog()
    }

    private fun onMinDurationModifyCheckFinished(filesToRemoveCount: Int, newMillis: Long) {
        if (filesToRemoveCount == 0) {
            applyAudioFileMinDurationMillisChange(newMillis)
        } else {
            showDialog(ConfirmMinDurationChangeDialog(filesToRemoveCount, newMillis))
        }
    }

    private fun applyAudioFileMinDurationMillisChange(millis: Long) {
        launch(onError = ::sendErrorMessage) {
            interactor.setAudioFileMinDurationMillis(millis)
        }
    }

    private fun onAllowedExtensionsModifyCheckFinished(filesToRemoveCount: Int, newExtensions: Set<String>) {
        if (filesToRemoveCount == 0) {
            applyAllowedFileExtensionsChange(newExtensions)
        } else {
            showDialog(ConfirmAllowedExtensionsChangeDialog(filesToRemoveCount, newExtensions))
        }
    }

    private fun applyAllowedFileExtensionsChange(extensions: Set<String>) {
        launch(onError = ::sendErrorMessage) {
            interactor.setAllowedFileExtensions(extensions)
        }
    }

}