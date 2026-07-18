package com.github.anrimian.musicplayer.ui.utils.compose.components.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.onSwipeContainer
import com.github.anrimian.musicplayer.ui.common.compose.swipeContainer
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util.SwipeActionPanel
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util.SwipeableContentWrapper
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util.detectSwipeAction
import com.github.anrimian.musicplayer.ui.utils.compose.performGestureThresholdActivate
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SingleActionSwipeContainer(
    onAction: () -> Unit,
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    panelWidth: Dp = 90.dp,
    dragLimitMultiplier: Float = 1.25f,
    swipedContentCorners: Dp = 8.dp,
    cornerRadiusAnimationDuration: Int = 120,
    containerColor: Color = MaterialTheme.colorScheme.swipeContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSwipeContainer,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current

    val isRtl = layoutDirection == LayoutDirection.Rtl

    val panelWidthPx = with(density) { panelWidth.toPx() }
    val maxDragLimitPx = panelWidthPx * dragLimitMultiplier

    val dragOffset = remember { Animatable(0f) }
    var isActivated by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        targetValue = if (isActivated) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "iconScale"
    )

    val isSwiping by remember { derivedStateOf { dragOffset.value < -1f } }

    val currentCornerRadius by animateDpAsState(
        targetValue = if (isSwiping) swipedContentCorners else 0.dp,
        animationSpec = tween(durationMillis = cornerRadiusAnimationDuration),
        label = "cornerRadius"
    )

    LaunchedEffect(isActivated) {
        if (isActivated) {
            hapticFeedback.performGestureThresholdActivate()
        }
    }

    val targetContainerColor = if (isSwiping) containerColor else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(targetContainerColor)
    ) {
        if (isSwiping) {
            val panelAlignment = if (isRtl) Alignment.CenterStart else Alignment.CenterEnd

            SwipeActionPanel(
                icon = icon,
                text = text,
                scale = iconScale,
                width = panelWidth,
                contentColor = contentColor,
                modifier = Modifier.align(panelAlignment)
            )
        }

        SwipeableContentWrapper(
            cornerRadius = currentCornerRadius,
            modifier = Modifier
                .offset {
                    val xOffset = if (isRtl) -dragOffset.value else dragOffset.value
                    IntOffset(xOffset.roundToInt(), 0)
                }
                .pointerInput(enabled, isRtl) {
                    if (!enabled) return@pointerInput
                    detectSwipeAction(
                        isRtl = isRtl,
                        onDrag = { delta ->
                            scope.launch {
                                val target = (dragOffset.value + delta).coerceIn(-maxDragLimitPx, 0f)
                                dragOffset.snapTo(target)
                                isActivated = abs(target) > panelWidthPx
                            }
                        },
                        onEnd = {
                            if (isActivated) {
                                onAction()
                            }
                            scope.launch {
                                dragOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                isActivated = false
                            }
                        },
                        onCancel = {
                            scope.launch {
                                dragOffset.animateTo(0f)
                                isActivated = false
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

