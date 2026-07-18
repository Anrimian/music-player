package com.github.anrimian.musicplayer.ui.playlists.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.utils.DialogViewModelContainer
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.CommonTextInputDialog
import com.github.anrimian.musicplayer.ui.common.compose.format
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit
) {
    DialogViewModelContainer<CreatePlaylistViewModel>(CreatePlaylistDialogData, onDismiss) { viewModel, dismiss ->
        val state by viewModel.state.collectAsStateWithLifecycle()

        ObserveEffects(viewModel.effects) { effect ->
            when (effect) {
                CreatePlaylistEffect.Close -> dismiss()
            }
        }

        CreatePlaylistDialogContent(
            state = state,
            onConfirm = viewModel::onConfirmClicked,
            onDismiss = dismiss
        )
    }
}

@Composable
private fun CreatePlaylistDialogContent(
    state: CreatePlaylistState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val error = state.error?.format(R.string.create_playlist_error_template)

    CommonTextInputDialog(
        title = stringResource(R.string.create_playlist),
        positiveText = stringResource(R.string.create),
        negativeText = stringResource(R.string.cancel),
        hint = stringResource(R.string.name),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isLoading = state.isLoading,
        error = error
    )
}
