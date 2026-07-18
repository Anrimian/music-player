package com.github.anrimian.musicplayer.ui.playlists.rename

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.utils.DialogViewModelContainer
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.CommonTextInputDialog
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects

@Composable
fun RenamePlaylistDialog(
    data: RenamePlaylistDialogData,
    onDismiss: () -> Unit
) {
    DialogViewModelContainer<RenamePlaylistViewModel>(data, onDismiss) { viewModel, dismiss ->
        val state by viewModel.state.collectAsStateWithLifecycle()

        ObserveEffects(viewModel.effects) { effect ->
            when (effect) {
                RenamePlaylistEffect.Close -> dismiss()
            }
        }

        RenamePlaylistDialogContent(
            initialName = data.initialName,
            state = state,
            onConfirm = viewModel::onConfirmClicked,
            onDismiss = dismiss
        )
    }
}

@Composable
private fun RenamePlaylistDialogContent(
    initialName: String,
    state: RenamePlaylistState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val error = state.error?.message?.let {
        stringResource(R.string.change_playlist_name_error_template, it)
    }

    CommonTextInputDialog(
        title = stringResource(R.string.edit_name),
        positiveText = stringResource(R.string.change),
        negativeText = stringResource(R.string.cancel),
        hint = stringResource(R.string.name),
        initialValue = initialName,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isLoading = state.isLoading,
        error = error
    )
}