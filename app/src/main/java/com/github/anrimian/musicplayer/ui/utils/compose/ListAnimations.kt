package com.github.anrimian.musicplayer.ui.utils.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

/**
 * Applies a consistent animation style for Lazy lists items (removal/reordering).
 * Requires [LazyItemScope] context.
 */
fun Modifier.appItemAnimation(
    scope: LazyItemScope,
    isDragging: Boolean = false
): Modifier {
    return with(scope) {
        this@appItemAnimation.animateItem(
            fadeInSpec = tween(durationMillis = 200),
            fadeOutSpec = tween(durationMillis = 100),
            placementSpec = if (isDragging) {
                null
            } else {
                spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )
            }
        )
    }
}

/*
// Alternative: Classic Material / RecyclerView Style
// More linear and predictable behavior using standard interpolators.
fun Modifier.classicItemAnimation(scope: LazyItemScope): Modifier {
    return with(scope) {
        this@classicItemAnimation.animateItem(
            fadeOutSpec = tween(
                durationMillis = 100,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            ),
            placementSpec = tween(
                durationMillis = 400,
                easing = androidx.compose.animation.core.LinearOutSlowInEasing
            )
        )
    }
}
*/