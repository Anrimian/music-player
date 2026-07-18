package com.github.anrimian.musicplayer.ui.utils.compose.components.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.utils.compose.performGestureThresholdActivate
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * A container that allows dismissing its content via a horizontal swipe gesture (similar to iOS/Telegram).
 * It features a physics-based "sticky" resistance, haptic feedback, and a visual "jump" effect when the threshold is reached.
 *
 * @param onDismiss Called when the swipe gesture is successfully completed and the close animation finishes.
 * @param modifier The modifier to be applied to the layout.
 * @param onDragProgress Callback that provides the current drag progress (0f to 1f). Useful for animating external elements (e.g. background dimming).
 * @param enabled Controls whether the swipe gesture is active.
 * @param dismissThreshold The fraction of the screen width (0.0 to 1.0) the user must drag to trigger a dismiss. Default is 0.3 (30%).
 * @param resistance Friction factor. Lower values make the content "heavier" to drag. Default is 0.75 (content lags 25% behind finger).
 * @param hysteresis The factor (0.0 to 1.0) of the threshold required to cancel the "dismiss" state. Prevents flickering when the finger is near the threshold.
 * @param maxCornerRadius The maximum corner radius applied to the content when dragging. Reaches maximum exactly at the [dismissThreshold].
 * @param dragScale The scaling factor applied during drag. E.g., 0.15 means the content will shrink down to 85% at full swipe.
 * @param scrimColor Color of the background overlay (dimming).
 * @param scrimMaxAlpha Maximum alpha value of the scrim when the swipe starts.
 * @param shadowWidth Width of the drop shadow gradient on the leading edge.
 * @param animationDurationMs Duration of the "fling" animation when dismissing or canceling.
 * @param content The composable content to be displayed inside the swipeable container.
 */
@Composable
fun SwipeToCloseContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDragProgress: (Float) -> Unit = {},
    enabled: Boolean = true,
    // Logic config
    dismissThreshold: Dp = 80.dp,
    resistance: Float = 0.75f,      // Drag resistance (0.75 = moves 25% slower than finger)
    hysteresis: Float = 0.65f,      // How far to pull back to cancel activation (0.65 = 65% of threshold)
    // Visual config
    maxCornerRadius: Dp = 18.dp,
    dragScale: Float = 0.12f,       // Max scale reduction (1.0 -> 0.85)
    scrimColor: Color = Color.Black,
    scrimMaxAlpha: Float = 0.7f,
    shadowWidth: Dp = 8.dp,
    animationDurationMs: Int = 200,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val windowInfo = LocalWindowInfo.current
    val hapticFeedback = LocalHapticFeedback.current

    val isRtl = layoutDirection == LayoutDirection.Rtl
    val screenWidthPx = with(density) { windowInfo.containerSize.width.dp.toPx() }

    val thresholdDpPx = with(density) { dismissThreshold.toPx() }

    // System gesture insets (Back navigation)
    val systemGestures = WindowInsets.systemGestures.asPaddingValues()
    val edgeGuardPx = with(density) {
        if (isRtl) systemGestures.calculateRightPadding(layoutDirection).toPx()
        else systemGestures.calculateLeftPadding(layoutDirection).toPx()
    }

    // Thresholds calculation
    val activationThresholdPx = thresholdDpPx
    val deactivationThresholdPx = activationThresholdPx * hysteresis

    // State
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isPastThreshold by remember { mutableStateOf(false) }

    // 0f..1f Progress relative to screen width
    val visualProgress by remember {
        derivedStateOf { (offsetX.value / screenWidthPx).coerceIn(0f, 1f) }
    }

    // "Pop" animation when threshold is passed
    val extraScale by animateFloatAsState(
        targetValue = if (isPastThreshold) 0.05f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "jump"
    )

    val scale by remember { derivedStateOf { 1f - (visualProgress * dragScale) - extraScale } }

    // Corners animate only until the threshold is reached.
    // Logic: Map progress (0..Threshold) to (0..MaxRadius). Cap at MaxRadius.
    val cornerRadius by remember {
        derivedStateOf {
            val thresholdProgress = (dismissThreshold * resistance)
            val normalized = (offsetX.value / activationThresholdPx).coerceIn(0f, 1f)
            maxCornerRadius * normalized
        }
    }

    LaunchedEffect(visualProgress) { onDragProgress(visualProgress) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Static container catches gestures to avoid coordinate distortion during scaling
            .pointerInput(enabled, isRtl, edgeGuardPx) {
                if (!enabled) return@pointerInput

                detectStickySwipe(
                    isRtl = isRtl,
                    edgeGuardPx = edgeGuardPx,
                    screenWidthPx = screenWidthPx,
                    onDrag = { delta ->
                        val sensitivity = if (isRtl) -1f else 1f
                        val dragDelta = delta * sensitivity * resistance

                        // Calculate target from targetValue to prevent async drift
                        val target = (offsetX.targetValue + dragDelta).coerceAtLeast(0f)

                        // Threshold logic with hysteresis
                        val newThresholdState = if (!isPastThreshold && target > activationThresholdPx) {
                            true
                        } else if (isPastThreshold && target < deactivationThresholdPx) {
                            false
                        } else {
                            isPastThreshold
                        }

                        // Haptic feedback on state change
                        if (newThresholdState != isPastThreshold) {
                            isPastThreshold = newThresholdState
                            if (newThresholdState) {
                                hapticFeedback.performGestureThresholdActivate()
                            }
                        }

                        scope.launch { offsetX.snapTo(target) }
                    },
                    onEnd = { velocity ->
                        val adjustedVel = if (isRtl) -velocity else velocity
                        val isFling = adjustedVel > 500f
                        val shouldDismiss = isFling || isPastThreshold

                        scope.launch {
                            if (shouldDismiss) {
                                offsetX.animateTo(screenWidthPx, tween(animationDurationMs))
                                onDismiss()
                            } else {
                                isPastThreshold = false
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    },
                    onCancel = {
                        scope.launch {
                            // Safety return. If we dragged past threshold, we commit to dismiss.
                            if (isPastThreshold) {
                                offsetX.animateTo(screenWidthPx, tween(animationDurationMs))
                                onDismiss()
                            } else {
                                isPastThreshold = false
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    }
                )
            }
    ) {
        // Scrim
        if (visualProgress > 0f && scrimMaxAlpha > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = scrimColor, alpha = (1f - visualProgress) * scrimMaxAlpha)
            }
        }

        // Animated Content
        Box(
            modifier = Modifier
                .offset {
                    val x = if (isRtl) -offsetX.value else offsetX.value
                    IntOffset(x.roundToInt(), 0)
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    // check for low values to prevent flickering on old apis(spotted on api 23, 24)
                    if (cornerRadius > 0.5.dp) {
                        shape = RoundedCornerShape(cornerRadius)
                        clip = true
                    }
                    transformOrigin = TransformOrigin(if (isRtl) 1f else 0f, 0.5f)
                }
        ) {
            // Shadow
            val shadowAlign = if (isRtl) Alignment.CenterEnd else Alignment.CenterStart
            val shadowVisualOffset = if (isRtl) shadowWidth else -shadowWidth
            val gradient = Brush.horizontalGradient(
                if (isRtl) listOf(Color.Black.copy(0.15f), Color.Transparent)
                else listOf(Color.Transparent, Color.Black.copy(0.15f))
            )

            Box(
                modifier = Modifier
                    .width(shadowWidth)
                    .fillMaxHeight()
                    .align(shadowAlign)
                    .offset(x = shadowVisualOffset)
                    .background(gradient)
            )

            content()
        }
    }
}

/**
 * Custom detector logic:
 * 1. Angle Bias (2:1 ratio) - forgives diagonal swipes.
 * 2. Dead Zone check - respects system gestures.
 * 3. Consumes touch slop to prevent parent interception.
 */
private suspend fun PointerInputScope.detectStickySwipe(
    isRtl: Boolean,
    edgeGuardPx: Float,
    screenWidthPx: Float,
    onDrag: (delta: Float) -> Unit,
    onEnd: (velocity: Float) -> Unit,
    onCancel: () -> Unit
) {
    val velocityTracker = VelocityTracker()

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val dragPointerId = down.id
        var overSlop = 0f

        var passedSlop = false
        var matchedDirection = false

        while (!passedSlop) {
            val event = awaitPointerEvent()
            val change = event.changes.find { inputChange -> inputChange.id == dragPointerId }

            if (change == null || change.isConsumed) {
                break
            }

            if (change.changedToUp() || change.isConsumed) {
                break
            }

            val dx = change.position.x - down.position.x
            val dy = change.position.y - down.position.y
            val dist = hypot(dx, dy)

            if (dist > viewConfiguration.touchSlop) {
                passedSlop = true

                val isStartEndSwipe = if (isRtl) dx < 0 else dx > 0
                val isHorizontal = abs(dx) > abs(dy) * 2 // ~26.5 degrees

                // Dead zone check (Back navigation gesture conflict)
                val startX = down.position.x
                val isInDeadZone = if (isRtl) startX > (screenWidthPx - edgeGuardPx) else startX < edgeGuardPx

                if (isStartEndSwipe && isHorizontal && !isInDeadZone) {
                    matchedDirection = true
                    overSlop = dx
                    change.consume()
                }
            }
        }

        if (passedSlop && matchedDirection) {
            velocityTracker.resetTracking()
            // Feed the initial movement that passed slop
            onDrag(overSlop)

            if (
                drag(dragPointerId) { change ->
                    change.consume()
                    velocityTracker.addPointerInputChange(change)
                    val delta = change.position.x - change.previousPosition.x
                    onDrag(delta)
                }
            ) {
                val velocity = velocityTracker.calculateVelocity().x
                onEnd(velocity)
            } else {
                onCancel()
            }
        }
    }
}