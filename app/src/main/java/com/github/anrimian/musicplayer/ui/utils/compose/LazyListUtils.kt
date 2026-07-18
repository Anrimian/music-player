package com.github.anrimian.musicplayer.ui.utils.compose

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first
import kotlin.math.abs

suspend fun LazyListState.awaitItemsPresence() {
    if (layoutInfo.visibleItemsInfo.isEmpty()) {
        snapshotFlow { layoutInfo.visibleItemsInfo.isNotEmpty() }
            .first { isItemsPresent -> isItemsPresent }
    }
}

suspend fun LazyListState.windowedScrollToPosition(
    position: Int,
    windowTopRatio: Float = 1f / 6f,
    windowBottomRatio: Float = 0.7f,
    isSnapRequested: Boolean = false,
    animateInvisible: Boolean = false,
    scrollVelocityPxPerMs: Float = 0.05f,
    minScrollDurationMs: Int = 170,
    maxScrollDurationMs: Int = 500,
) {
    val layoutInfo = this.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) {
        return
    }

    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val windowTop = (viewportHeight * windowTopRatio).toInt()
    val windowBottom = (viewportHeight * windowBottomRatio).toInt()

    val targetItem = visibleItems.find { info -> info.index == position }

    if (targetItem != null) {
        val itemTop = targetItem.offset
        val itemBottom = itemTop + targetItem.size

        val delta = when {
            itemTop < windowTop -> (itemTop - windowTop).toFloat()
            itemBottom > windowBottom -> (itemBottom - windowBottom).toFloat()
            else -> 0f
        }

        if (delta != 0f) {
            if (isSnapRequested) {
                val offset = if (itemTop < windowTop) -(windowTop) else -(windowBottom)
                this.scrollToItem(position, offset)
            } else {
                val durationMs = (abs(delta) / scrollVelocityPxPerMs).toInt()
                    .coerceIn(minScrollDurationMs, maxScrollDurationMs)
                val animationSpec = tween<Float>(
                    durationMillis = durationMs,
                    easing = LinearOutSlowInEasing
                )
                this.animateScrollBy(delta, animationSpec)
            }
        }
    } else {
        val firstVisibleIndex = visibleItems.first().index
        val offset = if (position < firstVisibleIndex) -(windowTop) else -(windowBottom)

        if (isSnapRequested || !animateInvisible) {
            this.scrollToItem(position, offset)
        } else {
            this.animateScrollToItem(position, offset)
        }
    }
}
