package com.github.anrimian.musicplayer.ui.common.compose.components.snackbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.snackbar.AppSnackbarVisuals

@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    actionColor: Color = MaterialTheme.colorScheme.inversePrimary
) {
    val visuals = snackbarData.visuals as? AppSnackbarVisuals
    val message = visuals?.message ?: snackbarData.visuals.message
    val actionLabel = visuals?.actionLabel ?: snackbarData.visuals.actionLabel

    Surface(
        modifier = modifier.padding(Dimens.snackbarPadding),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 8.dp,
                bottom = 8.dp,
                end = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (visuals != null && visuals.showProgressBar) {
                SnackbarTimer(
                    durationMillis = visuals.durationMillis,
                    color = actionColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = message,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            if (actionLabel != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { snackbarData.performAction() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = actionLabel,
                        color = actionColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun SnackbarTimer(
    durationMillis: Long,
    color: Color
) {
    val progress = remember { Animatable(1f) }

    LaunchedEffect(durationMillis) {
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = durationMillis.toInt(),
                easing = LinearEasing
            )
        )
    }

    CircularProgressIndicator(
        progress = { progress.value },
        modifier = Modifier.size(32.dp),
        color = color,
        strokeWidth = 3.dp,
        trackColor = Color.Transparent,
    )
}

//---------------

@Preview
@Composable
private fun AppSnackbarPreview() {
    PreviewAppTheme {
        AppSnackbar(
            snackbarData = object : SnackbarData {
                override val visuals = AppSnackbarVisuals(
                    message = "This is a sample message",
                    actionLabel = "Action",
                    duration = SnackbarDuration.Indefinite,
                    durationMillis = 5000,
                    showProgressBar = true
                )
                override fun performAction() {}
                override fun dismiss() {}
            }
        )
    }
}

@Preview
@Composable
private fun AppSnackbarLongTextPreview() {
    PreviewAppTheme {
        AppSnackbar(
            snackbarData = object : SnackbarData {
                override val visuals = AppSnackbarVisuals(
                    message = "This is a very long sample message that should wrap to multiple lines",
                    actionLabel = null,
                    duration = SnackbarDuration.Short,
                    durationMillis = 0,
                    showProgressBar = false
                )
                override fun performAction() {}
                override fun dismiss() {}
            }
        )
    }
}