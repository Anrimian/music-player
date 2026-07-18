package com.github.anrimian.musicplayer.ui.common.compose.components

import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import com.github.anrimian.musicplayer.ui.common.compose.selectionContainer
import com.google.android.material.progressindicator.LinearProgressIndicator

@Composable
fun SmoothLinearProgressIndicator(
    progress: Int,
    modifier: Modifier = Modifier,
    trackThickness: Dp = 2.dp,
    trackCornerRadius: Dp = 6.dp,
    indicatorColor: Int = MaterialTheme.colorScheme.primary.toArgb(),
    trackColor: Int = MaterialTheme.colorScheme.selectionContainer.toArgb(),
) {
    val density = LocalDensity.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LinearProgressIndicator(context).apply {
                this.trackThickness = with(density) { trackThickness.roundToPx() }
                this.trackCornerRadius = with(density) { trackCornerRadius.roundToPx() }
                
                this.setIndicatorColor(indicatorColor)
                this.trackColor = trackColor
            }
        },
        update = { view ->
            if (view.indicatorColor.firstOrNull() != indicatorColor) {
                view.setIndicatorColor(indicatorColor)
            }
            if (view.trackColor != trackColor) {
                view.trackColor = trackColor
            }

            if (progress < 0) {
                if (!view.isIndeterminate) {
                    view.indeterminate(true)
                }
            } else {
                if (view.isIndeterminate) {
                    view.indeterminate(false)
                }
                view.setProgressCompat(progress, true)
            }
        }
    )
}

private fun LinearProgressIndicator.indeterminate(isIndeterminate: Boolean) {
    if (this.isIndeterminate == isIndeterminate) {
        return
    }

    val wasVisible = isVisible
    if (wasVisible) {
        visibility = View.INVISIBLE
    }
    this.isIndeterminate = isIndeterminate
    if (wasVisible) {
        visibility = View.VISIBLE
    }
}