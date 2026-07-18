package com.github.anrimian.musicplayer.ui.library.common.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.PlaylistDuplicateEntriesDialog
import com.github.anrimian.musicplayer.ui.common.dialogs.share.ShareDialogData
import com.github.anrimian.musicplayer.ui.common.dialogs.share.ShareInteraction
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog

@Composable
fun LibraryDialogHost(
    viewModel: BaseLibraryViewModel<*, *>,
    renderSpecific: @Composable (AppDialog) -> Unit
) {
    val dialogs by viewModel.dialogStack.collectAsStateWithLifecycle()

    dialogs.forEach { dialog ->
        when (dialog) {
            is PlaylistDuplicateEntryDialog -> {
                PlaylistDuplicateEntriesDialog(
                    data = dialog,
                    onConfirm = viewModel::onAddDuplicatePlaylistEntriesConfirmed,
                    onCheckChange = viewModel::onPlaylistDuplicateChecked,
                    onDismiss = viewModel::onPlaylistDuplicateEntriesDialogClosed
                )
            }
            is ShareDialogData -> {
                ShareInteraction(
                    data = dialog,
                    onDismiss = viewModel::onShareDialogClosed,
                    onError = viewModel::onShareError
                )
            }
            else -> renderSpecific(dialog)
        }
    }
}