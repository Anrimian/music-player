package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.compositions

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel

class DeleteCompositionsViewModel(
    private val librarySettingsInteractor: LibrarySettingsInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : SimpleViewModel<DeleteCompositionsState>(
    initialState = DeleteCompositionsState(),
    savedStateHandle = savedStateHandle,
    errorParser = errorParser
) {

    init {
        val enabled = librarySettingsInteractor.isAppConfirmDeleteDialogEnabled()
        updateState { copy(isConfirmDeleteDialogEnabled = enabled) }
    }

    fun onEnableDialogCheckChanged(isChecked: Boolean) {
        val isEnabled = !isChecked
        librarySettingsInteractor.setAppConfirmDeleteDialogEnabled(isEnabled)
        updateState { copy(isConfirmDeleteDialogEnabled = isEnabled) }
    }

}