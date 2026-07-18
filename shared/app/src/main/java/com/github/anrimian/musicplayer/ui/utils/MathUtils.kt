package com.github.anrimian.musicplayer.ui.utils

/**
 * Normalizes the value within the [min, max] range to a float between 0f and 1f.
 *
 * If the value is outside the boundaries, it is clamped to 0f or 1f.
 * If min equals max, returns 0f to avoid division by zero.
 */
fun Float.fractionInRange(min: Float, max: Float): Float {
    if (max == min) return 0f
    val fraction = (this - min) / (max - min)
    return fraction.coerceIn(0f, 1f)
}