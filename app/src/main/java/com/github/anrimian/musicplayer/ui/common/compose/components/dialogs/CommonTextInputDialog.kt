package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.caption
import com.github.anrimian.musicplayer.ui.common.compose.captionError
import com.github.anrimian.musicplayer.ui.common.compose.editText
import com.github.anrimian.musicplayer.ui.common.compose.editTextHint
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.BaseDialog
import com.github.anrimian.musicplayer.ui.utils.compose.requestKeyboardFocus

@Composable
fun CommonTextInputDialog(
    title: String,
    positiveText: String,
    negativeText: String,
    hint: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    initialValue: String = "",
    canBeEmpty: Boolean = false,
    isLoading: Boolean = false,
    error: String? = null
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(initialValue.length)
            )
        )
    }

    val isEnabled = !isLoading && (canBeEmpty || textFieldValue.text.isNotBlank())

    BaseDialog(
        onDismissRequest = onDismiss,
        title = title,
        positiveText = positiveText,
        positiveAction = { if (isEnabled) onConfirm(textFieldValue.text) },
        positiveEnabled = isEnabled,
        negativeText = negativeText,
        negativeAction = onDismiss,
    ) {
        Column {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { value -> textFieldValue = value },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp)
                    .requestKeyboardFocus(),
                textStyle = MaterialTheme.typography.editText,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (isEnabled) onConfirm(textFieldValue.text) }
                ),
                singleLine = false,
                decorationBox = { innerTextField ->
                    Column {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.editTextHint,
                                )
                            }
                            innerTextField()
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            if (error != null) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = error,
                    style = MaterialTheme.typography.captionError,
                )
            }

            if (isLoading) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.create_progress),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}

//---------------------------

@Preview
@Composable
fun CommonTextInputDialogPreview() {
    PreviewAppTheme {
        CommonTextInputDialog(
            title = "Enter text",
            positiveText = "Ok",
            negativeText = "Cancel",
            hint = "Hint",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
fun CommonTextInputDialogLoadingPreview() {
    PreviewAppTheme {
        CommonTextInputDialog(
            title = "Enter text",
            positiveText = "Ok",
            negativeText = "Cancel",
            hint = "Hint",
            initialValue = "Initial value",
            onConfirm = {},
            onDismiss = {},
            isLoading = true
        )
    }
}

@Preview
@Composable
fun CommonTextInputDialogErrorPreview() {
    PreviewAppTheme {
        CommonTextInputDialog(
            title = "Enter text",
            positiveText = "Ok",
            negativeText = "Cancel",
            hint = "Hint",
            initialValue = "Initial value",
            onConfirm = {},
            onDismiss = {},
            error = "An error occurred"
        )
    }
}
