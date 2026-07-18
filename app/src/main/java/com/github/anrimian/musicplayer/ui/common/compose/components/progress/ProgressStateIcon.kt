package com.github.anrimian.musicplayer.ui.common.compose.components.progress

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme

@Composable
fun ProgressStateIcon(
    @DrawableRes iconRes: Int,
    progress: Float,
    showProgress: Boolean,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    strokeColor: Color = MaterialTheme.colorScheme.background
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = if (isVisible) 300 else 150),
        label = "VisibilityScale"
    )

    val isIndeterminate = progress < 0

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        if (scale > 0f) {
            ProgressStateCanvas(
                iconRes = iconRes,
                showProgress = showProgress,
                progressFraction = progress,
                isIndeterminate = isIndeterminate,
                contentColor = contentColor,
                backgroundColor = backgroundColor,
                strokeColor = strokeColor
            )
        }
    }
}

@Composable
private fun ProgressStateCanvas(
    @DrawableRes iconRes: Int,
    showProgress: Boolean,
    progressFraction: Float,
    isIndeterminate: Boolean,
    contentColor: Color,
    backgroundColor: Color,
    strokeColor: Color,
    progressBarPadding: Dp = 1.dp,
    progressStrokeWidth: Dp = 1.5.dp,
    outlineStrokeSize: Dp = 1.dp,
    iconPaddingExtra: Dp = 1.dp
) {

    val infiniteTransition = rememberInfiniteTransition(label = "ProgressGlobalRotation")
    val rotationState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val targetSweepAngle = if (isIndeterminate) {
        6f
    } else {
        (progressFraction * 360f).coerceAtLeast(6f)
    }

    val animatedSweepAngleState = animateFloatAsState(
        targetValue = targetSweepAngle,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        label = "SweepAngle"
    )

    val iconPainter = if (iconRes != 0) {
        rememberVectorPainter(ImageVector.vectorResource(iconRes))
    } else {
        null
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val radius = width / 2f
        val center = Offset(width / 2, width / 2)

        val outlineStrokePx = outlineStrokeSize.toPx()
        val progressBarPaddingPx = progressBarPadding.toPx()
        val progressStrokeWidthPx = progressStrokeWidth.toPx()
        val iconPaddingExtraPx = iconPaddingExtra.toPx()
        
        // 1. Outer Circle (Stroke)
        drawCircle(
            color = strokeColor,
            radius = radius
        )

        // 2. Inner Circle (Background)
        drawCircle(
            color = backgroundColor,
            radius = radius - outlineStrokePx
        )

        // 3. Icon
        val halfStroke = progressStrokeWidthPx / 2f
        val pbCenter = halfStroke + progressBarPaddingPx + outlineStrokePx
        val iconPadding = pbCenter + halfStroke + iconPaddingExtraPx
        val iconSize = width - (iconPadding * 2)

        if (iconSize > 0 && iconPainter != null) {
            inset(iconPadding) {
                with(iconPainter) {
                    draw(
                        size = Size(iconSize, iconSize),
                        colorFilter = ColorFilter.tint(contentColor)
                    )
                }
            }
        }

        // 4. Progress Arc
        if (showProgress) {
            val arcRadius = radius - pbCenter
            val arcDiameter = arcRadius * 2f
            val arcTopLeft = center - Offset(arcRadius, arcRadius)

            rotate(rotationState.value) {
                drawArc(
                    color = contentColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweepAngleState.value,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(arcDiameter, arcDiameter),
                    style = Stroke(
                        width = progressStrokeWidthPx,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun ProgressStateIconPreview() {
    PreviewAppTheme {
        ProgressStateIcon(
            modifier = Modifier.size(24.dp),
            iconRes = R.drawable.ic_download,
            progress = 0.5f,
            showProgress = true,
            isVisible = true
        )
    }
}
