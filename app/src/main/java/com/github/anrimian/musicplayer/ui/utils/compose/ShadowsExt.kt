package com.github.anrimian.musicplayer.ui.utils.compose

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.PopupShadowSurface(
    shadowAlpha: Float,
    shadowCornerRadius: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    val isLegacy = remember { useLegacyShadow() }

    val finalShadowRadius = if (isLegacy) shadowCornerRadius + 2.dp else shadowCornerRadius

    val finalModifier = if (isLegacy) {
        modifier.padding(vertical = 1.dp, horizontal = 0.5.dp)
    } else {
        modifier
    }

    PopupShadow(
        alpha = shadowAlpha,
        cornerRadius = finalShadowRadius,
        modifier = Modifier.matchParentSize()
    )

    Surface(
        modifier = finalModifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content
    )
}

fun Modifier.advancedShadow(
    color: Color = Color.Black,
    alpha: Float = 0.2f,
    borderRadius: Dp = 8.dp,
    shadowRadius: Dp = 4.dp,
    offsetY: Dp = 1.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    if (alpha <= 0f) {
        return@drawBehind
    }

    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparentColor = Color.Transparent.toArgb()

    val radiusPx = borderRadius.toPx()
    val shadowRadiusPx = shadowRadius.toPx()
    val safeShadowRadius = shadowRadiusPx.coerceAtLeast(1f)

    val offsetXPx = offsetX.toPx()
    val offsetYPx = offsetY.toPx()

    drawIntoCanvas { c ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        if (useLegacyShadow()) {
            frameworkPaint.color = shadowColor
            frameworkPaint.maskFilter = BlurMaskFilter(safeShadowRadius, BlurMaskFilter.Blur.NORMAL)

            c.drawRoundRect(
                left = offsetXPx,
                top = offsetYPx,
                right = size.width + offsetXPx,
                bottom = size.height + offsetYPx,
                radiusX = radiusPx,
                radiusY = radiusPx,
                paint = paint
            )
        } else {
            frameworkPaint.color = transparentColor
            frameworkPaint.setShadowLayer(
                shadowRadiusPx,
                offsetXPx,
                offsetYPx,
                shadowColor
            )
            c.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = radiusPx,
                radiusY = radiusPx,
                paint = paint
            )
        }
    }
}

@Composable
private fun PopupShadow(
    alpha: Float,
    cornerRadius: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .advancedShadow(
                color = Color.Black,
                alpha = alpha,
                borderRadius = cornerRadius,
                shadowRadius = 4.dp,
                offsetY = 1.dp
            )
    )
}

private fun useLegacyShadow() = Build.VERSION.SDK_INT < Build.VERSION_CODES.P