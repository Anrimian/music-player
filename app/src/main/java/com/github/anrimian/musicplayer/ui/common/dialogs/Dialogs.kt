package com.github.anrimian.musicplayer.ui.common.dialogs

import android.content.Context
import androidx.compose.runtime.Composable

open class Dialogs {

    open fun <R> showDeleteFileByImpactConfirmDialog(
        context: Context,
        filesToRemoveCount: Int,
        value: R,
        onConfirm: (R) -> Unit
    ) {
        onConfirm(value)
    }

    @Composable
    open fun DeleteFilesByImpactConfirmDialog(
        filesToRemoveCount: Int,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {}

}