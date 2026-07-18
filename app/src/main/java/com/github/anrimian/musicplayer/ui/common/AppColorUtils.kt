package com.github.anrimian.musicplayer.ui.common

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.cos
import kotlin.math.sin

fun isWhiteContrast(@ColorInt color: Int) = ColorUtils.calculateLuminance(color) >= 0.5f

/**
 * Generates a harmonious color based on the current color using CIELAB color space rotation.
 * This ensures all generated colors maintain the same perceived brightness and contrast ratio.
 *
 * @param index The index of the harmonious color to generate (e.g., role index).
 */
fun Color.generateHarmoniousColor(index: Int): Color {
    if (index == 0) {
        return this
    }

    val lab = DoubleArray(3)
    ColorUtils.colorToLAB(this.toArgb(), lab)
    val l = lab[0]
    val a = lab[1]
    val b = lab[2]

    // Rotate the a and b color channels by the golden angle (137.5 degrees)
    val angleRad = Math.toRadians(index * 137.5)
    val cosAngle = cos(angleRad)
    val sinAngle = sin(angleRad)

    val newA = a * cosAngle - b * sinAngle
    val newB = a * sinAngle + b * cosAngle

    return Color(ColorUtils.LABToColor(l, newA, newB))
}