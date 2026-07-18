package com.github.anrimian.musicplayer.ui.common.compose.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.labelExtraLarge

@Composable
fun AppButton(text: String, action: () -> Unit) {
    Button(onClick = action, shape = RoundedCornerShape(10.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppButtonLarge(text: String, action: () -> Unit) {
    Button(onClick = action, shape = RoundedCornerShape(10.dp)) {
        Text(text, style = MaterialTheme.typography.labelExtraLarge)
    }
}

@Composable
fun AppTextButton(text: String, action: () -> Unit) {
    TextButton(onClick = action, shape = RoundedCornerShape(10.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppTextButtonLarge(text: String, action: () -> Unit) {
    TextButton(onClick = action, shape = RoundedCornerShape(10.dp)) {
        Text(text, style = MaterialTheme.typography.labelExtraLarge)
    }
}

@Composable
fun AppOutlinedButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

//----------------------------------------------------------------------------------------------

@Preview
@Composable
private fun AppButtonPreview() {
    PreviewAppTheme {
        AppButton(text = "Test button", action = {})
    }
}

@Preview
@Composable
private fun AppButtonLargePreview() {
    PreviewAppTheme {
        AppButtonLarge(text = "Test button", action = {})
    }
}

@Preview
@Composable
private fun AppTextButtonPreview() {
    PreviewAppTheme {
        AppTextButton(text = "Test text button", action = {})
    }
}

@Preview
@Composable
private fun AppTextButtonLargePreview() {
    PreviewAppTheme {
        AppTextButtonLarge(text = "Test text button", action = {})
    }
}

@Preview
@Composable
private fun AppOutlinedButtonPreview() {
    PreviewAppTheme {
        AppOutlinedButton(text = "Test outlined button", onClick = {})
    }
}
