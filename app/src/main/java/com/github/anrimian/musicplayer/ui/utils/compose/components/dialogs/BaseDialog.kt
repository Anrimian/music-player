package com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.dialogButton
import com.github.anrimian.musicplayer.ui.common.compose.dialogTitle

@Composable
fun BaseDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    positiveText: String? = null,
    positiveAction: (() -> Unit)? = null,
    positiveEnabled: Boolean = true,
    title: String? = null,
    negativeText: String? = null,
    negativeAction: (() -> Unit)? = onDismissRequest,
    neutralText: String? = null,
    neutralAction: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimens.dialogContentHorizontalPadding),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = modifier
                .padding(horizontal = 20.dp)
                .widthIn(min = 280.dp, max = 560.dp)
                .fillMaxWidth()
        ) {
            Column {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.dialogTitle,
                        modifier = Modifier.padding(
                            start = Dimens.dialogContentHorizontalPadding,
                            end = Dimens.dialogContentHorizontalPadding,
                            top = Dimens.dialogContentVerticalPadding,
                            bottom = Dimens.dialogContentSpacingPadding
                        )
                    )
                } else {
                    Spacer(modifier = Modifier.height(Dimens.dialogContentVerticalPadding))
                }

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(contentPadding)
                ) {
                    Column(content = content)
                }

                Spacer(modifier = Modifier.height(Dimens.dialogContentSpacingPadding))

                val buttonShape = RoundedCornerShape(6.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (neutralText != null && neutralAction != null) {
                        TextButton(
                            onClick = neutralAction,
                            shape = buttonShape
                        ) {
                            Text(text = neutralText, style = MaterialTheme.typography.dialogButton)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (negativeText != null) {
                        TextButton(
                            onClick = negativeAction ?: onDismissRequest,
                            shape = buttonShape
                        ) {
                            Text(text = negativeText, style = MaterialTheme.typography.dialogButton)
                        }
                    }

                    if (positiveText != null) {
                        TextButton(
                            onClick = positiveAction ?: onDismissRequest,
                            shape = buttonShape,
                            enabled = positiveEnabled
                        ) {
                            Text(text = positiveText, style = MaterialTheme.typography.dialogButton)
                        }
                    }
                }
            }
        }
    }
}

//----------------------

@Preview
@Composable
fun BaseDialogPreview() {
    PreviewAppTheme {
        BaseDialog(
            onDismissRequest = {},
            title = "Dialog Title",
            positiveText = "Ok",
            negativeText = "Cancel",
            neutralText = "Later",
            neutralAction = {}
        ) {
            Text("This is the content of the dialog.")
        }
    }
}