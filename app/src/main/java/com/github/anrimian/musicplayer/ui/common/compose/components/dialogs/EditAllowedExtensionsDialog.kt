package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.ui.common.format.AppFormatUtils
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.AppTextInputDialog

@Composable
fun EditAllowedExtensionsDialog(
    currentExtensions: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val initialString = remember(currentExtensions) {
        AppFormatUtils.formatExtensions(currentExtensions)
    }

    AppTextInputDialog(
        initialValue = initialString,
        title = stringResource(R.string.allowed_file_extensions),
        confirmText = stringResource(R.string.change),
        negativeText = stringResource(R.string.cancel),
        neutralText = stringResource(R.string.restore_default),
        neutralAction = { onConfirm(Constants.DEFAULT_REMOTE_EXTENSIONS) },
        description = stringResource(R.string.allowed_file_extensions_description),
        onDismiss = onDismiss,
        onConfirm = { text ->
            val newExtensions = AppFormatUtils.parseExtensions(text)
            onConfirm(newExtensions)
        },
        canBeEmpty = true
    )
}