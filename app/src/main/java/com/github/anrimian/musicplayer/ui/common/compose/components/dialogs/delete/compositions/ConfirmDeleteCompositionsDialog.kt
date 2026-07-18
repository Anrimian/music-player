package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.compositions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.di.utils.DialogViewModelContainer
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.LocalDeviceCapabilities
import com.github.anrimian.musicplayer.ui.common.compose.components.DialogLabelledCheckbox
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.BaseDialog

@Composable
fun ConfirmDeleteCompositionsDialog(
    data: ConfirmDeleteCompositionsDialogData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogViewModelContainer<DeleteCompositionsViewModel>(data, onDismiss) { viewModel, dismiss ->
        val state by viewModel.state.collectAsStateWithLifecycle()

        val isConfirmDeleteDialogEnabled = state.isConfirmDeleteDialogEnabled
            ?: return@DialogViewModelContainer

        val hasDeleteFileDialog = LocalDeviceCapabilities.current.hasSystemDeleteFileDialog
        val hasExistingFiles = data.hasExistingFiles
        val isSystemDeleteLogicApplicable = hasDeleteFileDialog && hasExistingFiles

        val initiallyEnabled = remember { isConfirmDeleteDialogEnabled }
        val shouldAutoConfirm = isSystemDeleteLogicApplicable && !initiallyEnabled

        if (shouldAutoConfirm) {
            LaunchedEffect(Unit) {
                onConfirm()
            }
            return@DialogViewModelContainer
        }

        val formatter = remember { Components.getAppComponent().messageTextFormatter() }
        val message = formatter.getConfirmDeleteCompositionsText(data.ids.size, data.singleName)

        BaseDialog(
            title = stringResource(R.string.deleting),
            onDismissRequest = dismiss,
            positiveText = stringResource(R.string.delete),
            positiveAction = { onConfirm() },
            negativeText = stringResource(R.string.cancel),
            negativeAction = dismiss,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimens.dialogContentHorizontalPadding),
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (isSystemDeleteLogicApplicable) {
                DialogLabelledCheckbox(
                    isChecked = !isConfirmDeleteDialogEnabled,
                    onCheckedChange = viewModel::onEnableDialogCheckChanged,
                    label = stringResource(R.string.do_not_show_confirm_delete_dialog)
                )
            }
        }
    }
}