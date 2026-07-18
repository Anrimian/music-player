package com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppAlertDialog(
    message: String,
    onDismissRequest: () -> Unit,
    positiveText: String,
    positiveAction: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    negativeText: String? = null,
    negativeAction: (() -> Unit)? = onDismissRequest,
    neutralText: String? = null,
    neutralAction: (() -> Unit)? = null,
) {
    BaseDialog(
        onDismissRequest = onDismissRequest,
        positiveText = positiveText,
        positiveAction = positiveAction,
        modifier = modifier,
        title = title,
        negativeText = negativeText,
        negativeAction = negativeAction,
        neutralText = neutralText,
        neutralAction = neutralAction
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}