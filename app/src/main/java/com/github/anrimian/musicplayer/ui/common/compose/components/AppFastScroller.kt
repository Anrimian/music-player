package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppFastScroller(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    thumbColor: Color = MaterialTheme.colorScheme.outline,
    thumbWidth: Dp = 4.dp,
    thumbHeight: Dp = 48.dp,
    touchMinSize: Dp = 24.dp,
    minScrollToShow: Int = 10,
    autoHideDelayMillis: Long = 750L,
    animationDurationMillis: Int = 250
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val shouldShowScroller by remember {
        derivedStateOf {
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItemsCount = layoutInfo.totalItemsCount

            if (visibleItems.isEmpty() || totalItemsCount == 0) return@derivedStateOf false

            val averageItemSize = visibleItems.map { item -> item.size }.average().toFloat()
            if (averageItemSize <= 0f) return@derivedStateOf false

            val viewportHeight = layoutInfo.viewportSize.height -
                    layoutInfo.beforeContentPadding -
                    layoutInfo.afterContentPadding

            val estimatedVisibleCount = viewportHeight / averageItemSize
            totalItemsCount > (estimatedVisibleCount + minScrollToShow)
        }
    }
    if (!shouldShowScroller) return

    val totalItemsCount = layoutInfo.totalItemsCount

    var trackHeightPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onSizeChanged { size -> trackHeightPx = size.height.toFloat() }
    ) {
        if (trackHeightPx == 0f) return@Box

        val thumbHeightPx = with(density) { thumbHeight.toPx() }
        val topPaddingPx = with(density) { contentPadding.calculateTopPadding().toPx() }
        val bottomPaddingPx = with(density) { contentPadding.calculateBottomPadding().toPx() }

        val scrollRangePx = trackHeightPx - topPaddingPx - bottomPaddingPx - thumbHeightPx

        val derivedScrollableItemsCount by remember {
            derivedStateOf {
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return@derivedStateOf 1f

                val averageItemSize = visibleItems.map { item -> item.size }.average().toFloat()
                if (averageItemSize <= 0f) return@derivedStateOf 1f

                val viewportHeight = layoutInfo.viewportSize.height -
                        layoutInfo.beforeContentPadding -
                        layoutInfo.afterContentPadding

                val estimatedVisibleCount = viewportHeight / averageItemSize
                (totalItemsCount - estimatedVisibleCount).coerceAtLeast(1f)
            }
        }

        val thumbOffsetPx by remember {
            derivedStateOf {
                if (scrollRangePx <= 0f || totalItemsCount == 0) return@derivedStateOf topPaddingPx

                val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()
                    ?: return@derivedStateOf topPaddingPx

                val itemSize = firstVisibleItem.size.toFloat()
                val offsetInItem = listState.firstVisibleItemScrollOffset

                val exactIndex = firstVisibleItem.index + (offsetInItem.toFloat() / itemSize)
                val scrollProgress = exactIndex / derivedScrollableItemsCount

                val offset = (scrollProgress * scrollRangePx).coerceIn(0f, scrollRangePx)
                topPaddingPx + offset
            }
        }

        var isVisible by remember { mutableStateOf(false) }
        var isDragging by remember { mutableStateOf(false) }

        LaunchedEffect(listState.isScrollInProgress, isDragging) {
            if (isDragging) {
                isVisible = true
            } else if (listState.isScrollInProgress) {
                snapshotFlow {
                    (listState.firstVisibleItemIndex.toLong() shl 32) or
                            (listState.firstVisibleItemScrollOffset.toLong() and 0xFFFFFFFFL)
                }
                    .drop(1)
                    .collect { isVisible = true }
            } else {
                delay(autoHideDelayMillis)
                isVisible = false
            }
        }

        var dragOffset by remember { mutableFloatStateOf(0f) }

        val draggableState = rememberDraggableState { delta ->
            val min = topPaddingPx
            val max = topPaddingPx + scrollRangePx
            dragOffset = (dragOffset + delta).coerceIn(min, max)

            val progress = (dragOffset - topPaddingPx) / scrollRangePx
            val exactIndex = progress * derivedScrollableItemsCount

            val index = exactIndex.toInt()
            val remainder = exactIndex - index

            val averageSize = layoutInfo.visibleItemsInfo
                .map { item -> item.size }
                .average()
                .toFloat()
                .coerceAtLeast(1f)

            val scrollOffset = (remainder * averageSize).roundToInt()

            scope.launch {
                listState.scrollToItem(index, scrollOffset)
            }
        }

        if (!isDragging) {
            dragOffset = thumbOffsetPx
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { width ->
                    if (layoutDirection == LayoutDirection.Rtl) -width else width
                },
                animationSpec = tween(durationMillis = animationDurationMillis)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { width ->
                    if (layoutDirection == LayoutDirection.Rtl) -width else width
                },
                animationSpec = tween(durationMillis = animationDurationMillis)
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, dragOffset.roundToInt()) }
                    .width(max(thumbWidth, touchMinSize))
                    .height(thumbHeight)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = draggableState,
                        onDragStarted = { isDragging = true },
                        onDragStopped = { isDragging = false }
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Canvas(
                    modifier = Modifier
                        .width(thumbWidth)
                        .height(thumbHeight)
                ) {
                    val path = Path().apply {
                        val radius = CornerRadius(size.height / 2)
                        val isLtr = layoutDirection == LayoutDirection.Ltr

                        addRoundRect(
                            RoundRect(
                                rect = size.toRect(),
                                topLeft = if (isLtr) radius else CornerRadius.Zero,
                                bottomLeft = if (isLtr) radius else CornerRadius.Zero,
                                topRight = if (isLtr) CornerRadius.Zero else radius,
                                bottomRight = if (isLtr) CornerRadius.Zero else radius
                            )
                        )
                    }
                    drawPath(path = path, color = thumbColor)
                }
            }
        }
    }
}