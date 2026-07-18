package com.github.anrimian.musicplayer.ui.common.delete

import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.common.DeviceCapabilities
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.createDeleteCompleteMessage
import com.github.anrimian.musicplayer.ui.editor.common.EditorErrorCommand
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import kotlinx.coroutines.rx3.await

class FileDeletionHandler(
    private val playerInteractor: LibraryPlayerInteractor,
    private val storageFilesDataSource: StorageFilesDataSource,
    private val settingsInteractor: LibrarySettingsInteractor,
    private val deviceCapabilities: DeviceCapabilities
) {

    private var currentDeleteAction: (suspend () -> Unit)? = null

    suspend fun performFilesDelete(
        compositionsFetcher: suspend () -> List<CompositionModel>,
        outputEffects: (BaseEffect) -> Unit,
    ) {
        currentDeleteAction = {
            val compositionsToDelete = compositionsFetcher()
            val deletedCompositions = playerInteractor.deleteCompositions(compositionsToDelete).await()
            outputEffects(CommonEffect.ShowMessage(createDeleteCompleteMessage(deletedCompositions)))
            currentDeleteAction = null
        }
        runDeleteAction()
    }

    fun handleError(errorCommand: ErrorCommand, outputEffects: (BaseEffect) -> Unit) {
        val outputEffect = if (errorCommand is EditorErrorCommand) {
            ShowDeleteErrorEffect(errorCommand)
        } else {
            currentDeleteAction = null
            CommonEffect.ShowMessage(message = UiText.DynamicString(errorCommand.message))
        }
        outputEffects(outputEffect)
    }

    suspend fun onPermissionResult(
        isGranted: Boolean,
        outputEffects: (BaseEffect) -> Unit,
    ) {
        if (isGranted) {
            runDeleteAction()
        } else {
            storageFilesDataSource.clearDeleteData()
            currentDeleteAction = null

            val shouldShowMessage = !deviceCapabilities.hasSystemDeleteFileDialog
                    || settingsInteractor.isAppConfirmDeleteDialogEnabled()

            if (shouldShowMessage) {
                outputEffects(
                    CommonEffect.ShowMessage(
                        message = UiText.StringResource(R.string.android_r_edit_file_permission_denied)
                    )
                )
            }
        }
    }

    private suspend fun runDeleteAction() {
        currentDeleteAction?.invoke()
    }

}