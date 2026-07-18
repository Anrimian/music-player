package com.github.anrimian.musicplayer.ui.utils.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.abs

@Composable
fun <T> WheelPicker(
    values: List<T>,
    initialValue: T,
    onValueChanged: (T) -> Unit,
    valueFormatter: (T) -> String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    itemHeight: Dp = 48.dp,
    visibleItemsCount: Int = 3,
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val totalHeight = itemHeight * visibleItemsCount
    // Padding to center the selected item
    val verticalPadding = itemHeight * ((visibleItemsCount - 1) / 2)

    // --- Infinite Scroll Logic ---
    val infiniteCount = Int.MAX_VALUE
    val initialItemIndex = values.indexOf(initialValue).coerceAtLeast(0)
    val initialScrollIndex = remember(values) {
        val middle = infiniteCount / 2
        middle - (middle % values.size) + initialItemIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // --- Center Detection ---
    // Finds which item index is currently closest to the center of the viewport
    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf -1

            val viewportCenter = layoutInfo.viewportSize.height / 2

            val closestItem = visibleItems.minByOrNull {
                // Correctly calculate distance to center considering padding
                val itemCenter = it.offset + layoutInfo.beforeContentPadding + (it.size / 2)
                abs(itemCenter - viewportCenter)
            }
            closestItem?.index ?: -1
        }
    }

    LaunchedEffect(centeredIndex) {
        if (centeredIndex != -1) {
            val realIndex = centeredIndex % values.size
            val newValue = values[realIndex]

            // Trigger haptic only during user interaction/scroll
            if (listState.isScrollInProgress) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onValueChanged(newValue)
            } else {
                // Ensure state is updated after fling/snap settles
                onValueChanged(newValue)
            }
        }
    }

    Box(
        modifier = modifier
            .height(totalHeight)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Dividers
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(itemHeight)
        ) {
            val dividerColor = MaterialTheme.colorScheme.outlineVariant
            HorizontalDivider(
                color = dividerColor,
                thickness = 1.dp,
                modifier = Modifier.width(70.dp)
            )
            HorizontalDivider(
                color = dividerColor,
                thickness = 1.dp,
                modifier = Modifier.width(70.dp)
            )
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = verticalPadding),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                count = infiniteCount,
                key = { index -> index }
            ) { index ->
                val realIndex = index % values.size

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val layoutInfo = listState.layoutInfo
                            val viewportCenter = layoutInfo.viewportSize.height / 2f
                            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }

                            if (itemInfo != null) {
                                // Calculate distance relative to the viewport center
                                val itemCenter = itemInfo.offset + layoutInfo.beforeContentPadding + (itemInfo.size / 2f)
                                val dist = abs(viewportCenter - itemCenter)

                                // Normalize distance based on item height
                                val fraction = (dist / itemHeightPx).coerceIn(0f, 1f)

                                val scale = lerp(1f, 0.75f, fraction)
                                scaleX = scale
                                scaleY = scale
                                alpha = lerp(1f, 0.6f, fraction)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = valueFormatter(values[realIndex]),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Fading Edge Gradients
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(containerColor, containerColor.copy(alpha = 0f))
                        )
                    )
            )
            Spacer(modifier = Modifier.height(itemHeight))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(containerColor.copy(alpha = 0f), containerColor)
                        )
                    )
            )
        }
    }
}