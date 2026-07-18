package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.AppAlertDialog

@Composable
fun ConfirmDeletePlaylistDialog(
    data: ConfirmDeletePlaylistDialogData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = if (data.singleName != null) {
        stringResource(R.string.delete_playlist_template, data.singleName)
    } else {
        pluralStringResource(R.plurals.delete_playlists_template, data.ids.size, data.ids.size)
    }

    ConfirmDeleteDialog(
        message = message,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ConfirmDeleteDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        title = stringResource(R.string.deleting),
        message = message,
        onDismissRequest = onDismiss,
        positiveText = stringResource(R.string.delete),
        positiveAction = onConfirm,
        negativeText = stringResource(R.string.cancel),
        negativeAction = onDismiss
    )
}