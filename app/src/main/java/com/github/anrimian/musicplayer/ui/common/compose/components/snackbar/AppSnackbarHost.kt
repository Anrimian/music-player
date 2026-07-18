package com.github.anrimian.musicplayer.ui.common.compose.components.snackbar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun AppSnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(snackbarHostState, modifier) { data ->
        val dismissState = rememberSwipeToDismissBoxState()

        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                data.dismiss()
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {}
        ) {
            AppSnackbar(
                modifier = Modifier
                    .graphicsLayer {
                        val currentOffset = try {
                            dismissState.requireOffset()
                        } catch (_: Exception) {
                            0f
                        }

                        val width = size.width
                        if (width > 0f) {
                            val fraction = currentOffset.absoluteValue / width

                            val fadeThreshold = 0.5f
                            val alphaFraction = (fraction / fadeThreshold).coerceIn(0f, 1f)
                            alpha = 1f - alphaFraction

                            val scaleFraction = 1f - (fraction * 0.15f)
                            scaleX = scaleFraction
                            scaleY = scaleFraction
                        } else {
                            alpha = 1f
                        }
                    },
                snackbarData = data,
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}
