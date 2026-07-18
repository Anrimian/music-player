package com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.caption
import com.github.anrimian.musicplayer.ui.common.compose.editText
import com.github.anrimian.musicplayer.ui.common.compose.editTextHint
import com.github.anrimian.musicplayer.ui.utils.compose.requestKeyboardFocus

@Composable
fun AppTextInputDialog(
    initialValue: String,
    title: String,
    hint: String? = null,
    description: String? = null,
    confirmText: String,
    onConfirm: (String) -> Unit,
    negativeText: String,
    onDismiss: () -> Unit,
    neutralText: String? = null,
    neutralAction: (() -> Unit)? = null,
    canBeEmpty: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done
    ),
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(initialValue.length)
            )
        )
    }

    val isEnabled = canBeEmpty || textFieldValue.text.isNotBlank()

    BaseDialog(
        onDismissRequest = onDismiss,
        title = title,
        positiveText = confirmText,
        positiveAction = { if (isEnabled) onConfirm(textFieldValue.text) },
        negativeText = negativeText,
        negativeAction = onDismiss,
        neutralText = neutralText,
        neutralAction = neutralAction
    ) {
        Column {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp)
                    .requestKeyboardFocus(),
                textStyle = MaterialTheme.typography.editText,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = keyboardOptions,
                keyboardActions = KeyboardActions(
                    onDone = { if (isEnabled) onConfirm(textFieldValue.text) }
                ),
                singleLine = false,
                decorationBox = { innerTextField ->
                    Column {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (textFieldValue.text.isEmpty() && hint != null) {
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

            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}