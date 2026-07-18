package com.github.anrimian.musicplayer.ui.common.compose

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun calculateRippleConfiguration(isDark: Boolean): RippleConfiguration {
    val defaultAlpha = RippleAlpha

    return remember(isDark) {
        if (isDark) {
            RippleConfiguration(
                color = Color.Unspecified,
                rippleAlpha = RippleAlpha(
                    draggedAlpha = defaultAlpha.draggedAlpha,
                    focusedAlpha = defaultAlpha.focusedAlpha,
                    hoveredAlpha = defaultAlpha.hoveredAlpha,
                    pressedAlpha = 0.20f
                )
            )
        } else {
            RippleConfiguration()
        }
    }
}