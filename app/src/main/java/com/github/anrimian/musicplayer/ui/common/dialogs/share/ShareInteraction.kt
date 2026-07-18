package com.github.anrimian.musicplayer.ui.common.dialogs.share

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.utils.DialogViewModelContainer
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.captionError
import com.github.anrimian.musicplayer.ui.common.compose.components.SmoothLinearProgressIndicator
import com.github.anrimian.musicplayer.ui.common.compose.components.buttons.AppOutlinedButton
import com.github.anrimian.musicplayer.ui.common.compose.contentSubtitleMedium
import com.github.anrimian.musicplayer.ui.common.dialogs.launchShareSourcesActivity
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.BaseDialog
import com.github.anrimian.musicplayer.ui.utils.compose.preview.LightDarkPreview

@Composable
fun ShareInteraction(
    data: ShareDialogData,
    onDismiss: () -> Unit,
    onError: (ErrorCommand) -> Unit
) {
    DialogViewModelContainer<ShareViewModel>(data, onDismiss) { viewModel, dismiss ->
        
        val state by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current

        ObserveEffects(viewModel.effects) { effect ->
            when (effect) {
                ShareEffect.Close -> dismiss()
                is ShareEffect.Error -> {
                    onError(effect.error)
                    dismiss()
                }
                is ShareEffect.Share -> {
                    launchShareSourcesActivity(context, effect.sources)
                    dismiss()
                }
            }
        }

        if (state.isInLoadingMode) {
            ShareProgressDialog(
                state = state,
                onCancel = viewModel::onCancel,
                onRetry = viewModel::onRetry
            )
        }
    }
}

@Composable
private fun ShareProgressDialog(
    state: ShareState,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    val isError = state.error != null

    BaseDialog(
        onDismissRequest = onCancel,
        title = stringResource(R.string.preparing_files),
        negativeText = stringResource(R.string.cancel),
        negativeAction = onCancel
    ) {
        Column {
            if (isError) {
                Text(
                    text = state.error.message,
                    style = MaterialTheme.typography.captionError,
                )
                AppOutlinedButton(
                    text = stringResource(R.string.try_again),
                    onClick = onRetry
                )
            } else {
                Text(
                    text = stringResource(R.string.downloading, state.prepared, state.total),
                    style = MaterialTheme.typography.contentSubtitleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                SmoothLinearProgressIndicator(
                    progress = state.progress,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

//-----------------

@Preview(name = "1. Loading (45%)", showBackground = true)
@Composable
private fun PreviewShareProgress_Determinate() {
    PreviewAppTheme {
        ShareProgressDialog(
            state = ShareState(
                isInLoadingMode = true,
                progress = 55,
                prepared = 4,
                total = 10
            ),
            onCancel = {},
            onRetry = {}
        )
    }
}

@Preview(name = "2. Loading (Indeterminate)", showBackground = true)
@Composable
private fun PreviewShareProgress_Indeterminate() {
    PreviewAppTheme {
        ShareProgressDialog(
            state = ShareState(
                isInLoadingMode = true,
                progress = -1,
                prepared = 0,
                total = 10
            ),
            onCancel = {},
            onRetry = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun PreviewShareProgress_Error() {
    PreviewAppTheme {
        ShareProgressDialog(
            state = ShareState(
                isInLoadingMode = false,
                error = ErrorCommand("Connection timed out")
            ),
            onCancel = {},
            onRetry = {}
        )
    }
}