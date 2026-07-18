package com.github.anrimian.musicplayer.ui.utils.compose.components.swipe


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.github.anrimian.musicplayer.ui.common.compose.swipeContainerActivated
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util.SwipeActionPanel
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util.SwipeableContentWrapper
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util.detectSwipeAction
import com.github.anrimian.musicplayer.ui.utils.compose.performGestureThresholdActivate
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MultiActionSwipeContainer(
    secondActionIcon: Painter,
    secondActionText: String,
    onSecondAction: () -> Unit,
    modifier: Modifier = Modifier,
    firstActionIcon: Painter? = null,
    firstActionText: String? = null,
    onFirstAction: (() -> Unit)? = null,
    enabled: Boolean = true,
    panelWidth: Dp = 90.dp,
    secondActionThresholdMultiplier: Float = 2.1f,
    swipedContentCorners: Dp = 8.dp,
    cornerRadiusAnimationDuration: Int = 120,
    defaultBgColor: Color = MaterialTheme.colorScheme.swipeContainer,
    defaultContentColor: Color = MaterialTheme.colorScheme.onSwipeContainer,
    secondActionBgColor: Color = MaterialTheme.colorScheme.swipeContainerActivated,
    secondActionContentColor: Color = MaterialTheme.colorScheme.onSwipeContainer,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current

    val isRtl = layoutDirection == LayoutDirection.Rtl
    val hasFirstAction = onFirstAction != null

    val currentOnSecondAction by rememberUpdatedState(onSecondAction)
    val currentOnFirstAction by rememberUpdatedState(onFirstAction)

    val panelWidthPx = with(density) { panelWidth.toPx() }

    val secondThresholdPx = if (hasFirstAction) {
        panelWidthPx * secondActionThresholdMultiplier
    } else {
        panelWidthPx
    }

    val dragOffset = remember { Animatable(0f) }

    var activeAction by remember { mutableStateOf(SwipeActionState.NONE) }

    val visualAction = if (activeAction != SwipeActionState.NONE) {
        activeAction
    } else {
        if (hasFirstAction) SwipeActionState.FIRST else SwipeActionState.SECOND
    }

    val iconScale by animateFloatAsState(
        targetValue = if (activeAction != SwipeActionState.NONE) {
            // slightly shift target to force bounce animation replace
            if (activeAction == SwipeActionState.SECOND) 1.0001f else 1f
        } else {
            0f
        },
        animationSpec = if (activeAction == SwipeActionState.SECOND) {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        } else {
             spring(
                 dampingRatio = Spring.DampingRatioMediumBouncy,
                 stiffness = Spring.StiffnessMediumLow
             )
        },
        label = "iconScale"
    )

    val isSwiping by remember { derivedStateOf { dragOffset.value < -1f } }

    val currentCornerRadius by animateDpAsState(
        targetValue = if (isSwiping) swipedContentCorners else 0.dp,
        animationSpec = tween(durationMillis = cornerRadiusAnimationDuration),
        label = "cornerRadius"
    )




    val targetContainerColor = when {
        activeAction == SwipeActionState.SECOND -> secondActionBgColor
        activeAction == SwipeActionState.FIRST -> defaultBgColor
        isSwiping -> defaultBgColor
        else -> Color.Transparent
    }

    val containerColor by animateColorAsState(targetContainerColor, label = "containerColor")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor)
    ) {
        val contentColor by animateColorAsState(
            if (activeAction == SwipeActionState.SECOND) secondActionContentColor else defaultContentColor,
            label = "contentColor"
        )

        if (isSwiping || activeAction != SwipeActionState.NONE) {
            AnimatedContent(
                targetState = visualAction,
                transitionSpec = {
                    (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
                },
                modifier = Modifier
                    .align(if (isRtl) Alignment.CenterStart else Alignment.CenterEnd)
                    .alpha(if (isSwiping) 1f else 0f),
                label = "actionContent"
            ) { state ->
                val showSecondAction = state == SwipeActionState.SECOND
                val icon = if (showSecondAction) secondActionIcon else firstActionIcon ?: secondActionIcon
                val text = if (showSecondAction) secondActionText else firstActionText ?: secondActionText

                SwipeActionPanel(
                    icon = icon,
                    text = text,
                    scale = iconScale,
                    width = panelWidth,
                    contentColor = contentColor
                )
            }
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
                                val target = (dragOffset.value + delta).coerceAtMost(0f)
                                dragOffset.snapTo(target)

                                val absOffset = abs(target)
                                val newAction = when {
                                    absOffset > secondThresholdPx -> SwipeActionState.SECOND
                                    hasFirstAction && absOffset > panelWidthPx -> SwipeActionState.FIRST
                                    else -> SwipeActionState.NONE
                                }
                                if (activeAction != newAction) {
                                    activeAction = newAction
                                    if (newAction != SwipeActionState.NONE) {
                                         hapticFeedback.performGestureThresholdActivate()
                                    }
                                }
                            }
                        },
                        onEnd = { velocity ->
                            val absOffset = abs(dragOffset.value)
                            
                            // 0.2f weight means "where it would be in 200ms"
                            val projectedOffset = dragOffset.value + velocity * 0.2f
                            val absProjected = abs(projectedOffset)
                            
                            when {
                                absProjected > secondThresholdPx || absOffset > secondThresholdPx -> {
                                    scope.launch {
                                        val previousAction = activeAction
                                        activeAction = SwipeActionState.SECOND
                                        if (previousAction != SwipeActionState.SECOND && previousAction != SwipeActionState.FIRST) {
                                            hapticFeedback.performGestureThresholdActivate()
                                        }

                                        val target = -size.width.toFloat() * 2
                                        val startValue = dragOffset.value
                                        val totalDistance = abs(target - startValue)
                                        var actionTriggered = false
                                        
                                        dragOffset.animateTo(
                                            targetValue = target,
                                            animationSpec = tween(durationMillis = 300)
                                        ) {
                                            if (!actionTriggered) {
                                                val currentDistance = abs(value - startValue)
                                                if (currentDistance / totalDistance >= 0.65f) {
                                                    currentOnSecondAction()
                                                    actionTriggered = true
                                                }
                                            }
                                        }
                                        if (!actionTriggered) {
                                            currentOnSecondAction()
                                        }
                                    }
                                }
                                hasFirstAction && (absProjected > panelWidthPx || absOffset > panelWidthPx) -> {
                                    scope.launch {
                                        if (activeAction == SwipeActionState.NONE) {
                                            hapticFeedback.performGestureThresholdActivate()
                                        }
                                        currentOnFirstAction?.invoke()
                                        dragOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                        activeAction = SwipeActionState.NONE
                                    }
                                }
                                else -> {
                                    scope.launch {
                                        dragOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                        activeAction = SwipeActionState.NONE
                                    }
                                }
                            }
                        },
                        onCancel = {
                            scope.launch {
                                dragOffset.animateTo(0f)
                                activeAction = SwipeActionState.NONE
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

private enum class SwipeActionState {
    NONE,
    FIRST,
    SECOND
}



