package com.github.anrimian.musicplayer.ui.common.compose

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape

data class AppShapes(
    val coverShape: Shape = RoundedCornerShape(35)
)

val LocalAppShapes = staticCompositionLocalOf { AppShapes() }

fun calculateAppShapes(isCircleShape: Boolean): AppShapes {
    val shape = if (isCircleShape) CircleShape else RoundedCornerShape(35)
    return AppShapes(coverShape = shape)
}