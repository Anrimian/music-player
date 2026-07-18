package com.github.anrimian.musicplayer.ui.common.compose.components.popup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.github.anrimian.musicplayer.ui.utils.compose.PopupShadowSurface

@Composable
fun AppPopupWindow(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalStrategy: HorizontalStrategy = HorizontalStrategy.StartAligned,
    verticalStrategy: VerticalStrategy = VerticalStrategy.BottomOutside,
    offset: DpOffset = DpOffset.Zero,
    cornerRadius: Dp = 8.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    screenPadding: Dp = 8.dp,
    useSmartPivot: Boolean = false,
    content: @Composable () -> Unit
) {
    var isOpen by remember { mutableStateOf(expanded) }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(expanded) {
        if (expanded) {
            isOpen = true
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 235, easing = LinearEasing)
            )
        } else {
            if (isOpen) {
                animProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 150, easing = LinearEasing)
                )
                isOpen = false
            }
        }
    }

    if (!isOpen) return

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val configuration = LocalConfiguration.current

    val fullScreenWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val fullScreenHeight = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    val safeInsets = WindowInsets.safeDrawing
    val leftInset = safeInsets.getLeft(density, layoutDirection)
    val topInset = safeInsets.getTop(density)
    val rightInset = safeInsets.getRight(density, layoutDirection)
    val bottomInset = safeInsets.getBottom(density)

    val safeAreaRect = IntRect(
        left = leftInset,
        top = topInset,
        right = fullScreenWidth - rightInset,
        bottom = fullScreenHeight - bottomInset
    )

    val maxContentWidth = with(density) {
        (safeAreaRect.width - (screenPadding.roundToPx() * 2)).toDp()
    }

    val offsetPx = IntOffset(
        x = with(density) { offset.x.roundToPx() },
        y = with(density) { offset.y.roundToPx() }
    )

    val popupPositionProvider = remember(
        offsetPx,
        density,
        horizontalStrategy,
        verticalStrategy,
        safeAreaRect,
        screenPadding
    ) {
        UniversalCenterProvider(
            horizontalStrategy = horizontalStrategy,
            verticalStrategy = verticalStrategy,
            contentOffset = offsetPx,
            density = density,
            safeArea = safeAreaRect,
            screenPadding = screenPadding,
            layoutDirection = layoutDirection
        )
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true, clippingEnabled = false),
        content = {
            val transformOrigin = remember(horizontalStrategy, verticalStrategy, layoutDirection, useSmartPivot) {
                val isLtr = layoutDirection == LayoutDirection.Ltr
                if (useSmartPivot) {
                    val pivotX = when (horizontalStrategy) {
                        HorizontalStrategy.StartOutside -> if (isLtr) 1f else 0f
                        HorizontalStrategy.EndOutside -> if (isLtr) 0f else 1f
                        HorizontalStrategy.StartAligned -> if (isLtr) 0f else 1f
                        HorizontalStrategy.EndAligned -> if (isLtr) 1f else 0f
                        HorizontalStrategy.CenterAligned -> 0.5f
                    }
                    val pivotY = when (verticalStrategy) {
                        VerticalStrategy.BottomOutside -> 0f
                        VerticalStrategy.TopOutside -> 1f
                        VerticalStrategy.TopAligned -> 0f
                        VerticalStrategy.BottomAligned -> 1f
                        VerticalStrategy.CenterAligned -> 0.5f
                    }
                    TransformOrigin(pivotX, pivotY)
                } else {
                    val pivotX = if (isLtr) 1f else 0f
                    val pivotY = 1f
                    TransformOrigin(pivotX, pivotY)
                }
            }

            Box(
                modifier = Modifier.graphicsLayer {
                    val linearProgress = animProgress.value

                    if (expanded) {
                        val scaleT = ClassicOvershootEasing.transform(linearProgress)
                        val scale = 0.95f + (0.05f * scaleT)
                        scaleX = scale
                        scaleY = scale
                    } else {
                        scaleX = 1f
                        scaleY = 1f
                    }

                    this.transformOrigin = transformOrigin
                }
            ) {
                val linearProgress = animProgress.value
                val contentAlpha = if (expanded) {
                    (linearProgress / 0.75f).coerceIn(0f, 1f)
                } else {
                    AccelerateEasing.transform(linearProgress)
                }

                PopupShadowSurface(
                    shadowAlpha = 0.2f * contentAlpha,
                    shadowCornerRadius = cornerRadius,
                    modifier = modifier
                        .widthIn(max = maxContentWidth)
                        .graphicsLayer {
                            val linearProgress = animProgress.value
                            alpha = if (expanded) {
                                (linearProgress / 0.75f).coerceIn(0f, 1f)
                            } else {
                                AccelerateEasing.transform(linearProgress)
                            }
                        },
                    shape = RoundedCornerShape(cornerRadius),
                    color = containerColor,
                    shadowElevation = 0.dp
                ) {
                    content()
                }
            }
        }
    )
}

private class UniversalCenterProvider(
    private val horizontalStrategy: HorizontalStrategy,
    private val verticalStrategy: VerticalStrategy,
    private val contentOffset: IntOffset,
    private val density: Density,
    private val safeArea: IntRect,
    private val screenPadding: Dp,
    private val layoutDirection: LayoutDirection
) : PopupPositionProvider {

    private var isInitialized = false
    private var fixedCenterX = 0
    private var fixedTopY = 0

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val screenPaddingPx = with(density) { screenPadding.roundToPx() }
        val minX = safeArea.left + screenPaddingPx
        val maxX = safeArea.right - screenPaddingPx
        val minY = safeArea.top + screenPaddingPx
        val maxY = safeArea.bottom - screenPaddingPx

        if (!isInitialized) {
            val initialWidthPx = popupContentSize.width
            val initialHeight = popupContentSize.height

            val finalHStrat = resolveHorizontalStrategy(anchorBounds, initialWidthPx, safeArea)
            var targetLeftX = calculateBaseX(finalHStrat, anchorBounds, initialWidthPx) + contentOffset.x

            // Initial Clamp
            if (targetLeftX < minX) {
                targetLeftX = minX
            }
            if (targetLeftX + initialWidthPx > maxX) {
                targetLeftX = maxX - initialWidthPx
            }

            fixedCenterX = targetLeftX + (initialWidthPx / 2)

            val finalVStrat = resolveVerticalStrategy(anchorBounds, initialHeight, safeArea)
            var targetTopY = calculateBaseY(finalVStrat, anchorBounds, initialHeight) + contentOffset.y

            if (targetTopY + initialHeight > maxY) {
                targetTopY = maxY - initialHeight
            }
            if (targetTopY < minY) {
                targetTopY = minY
            }

            fixedTopY = targetTopY
            isInitialized = true
        }

        val currentW = popupContentSize.width
        val currentH = popupContentSize.height

        var x = fixedCenterX - (currentW / 2)
        var y = fixedTopY

        // Hard Clamp (in case content expands during swipe)
        if (x < minX) {
            x = minX
        } else if (x + currentW > maxX) {
            x = maxX - currentW
        }

        if (y < minY) {
            y = minY
        } else if (y + currentH > maxY) {
            y = maxY - currentH
        }

        return IntOffset(x, y)
    }

    private fun resolveHorizontalStrategy(anchor: IntRect, width: Int, safeArea: IntRect): HorizontalStrategy {
        if (checkFitsX(horizontalStrategy, anchor, width, safeArea)) {
            return horizontalStrategy
        }
        return when (horizontalStrategy) {
            HorizontalStrategy.StartOutside -> if (checkFitsX(HorizontalStrategy.EndOutside, anchor, width, safeArea)) HorizontalStrategy.EndOutside else HorizontalStrategy.CenterAligned
            HorizontalStrategy.EndOutside -> if (checkFitsX(HorizontalStrategy.StartOutside, anchor, width, safeArea)) HorizontalStrategy.StartOutside else HorizontalStrategy.CenterAligned
            HorizontalStrategy.StartAligned -> if (checkFitsX(HorizontalStrategy.EndAligned, anchor, width, safeArea)) HorizontalStrategy.EndAligned else HorizontalStrategy.CenterAligned
            HorizontalStrategy.EndAligned -> if (checkFitsX(HorizontalStrategy.StartAligned, anchor, width, safeArea)) HorizontalStrategy.StartAligned else HorizontalStrategy.CenterAligned
            HorizontalStrategy.CenterAligned -> HorizontalStrategy.CenterAligned
        }
    }

    private fun resolveVerticalStrategy(anchor: IntRect, height: Int, safeArea: IntRect): VerticalStrategy {
        if (checkFitsY(verticalStrategy, anchor, height, safeArea)) {
            return verticalStrategy
        }
        return when (verticalStrategy) {
            VerticalStrategy.BottomOutside -> if (checkFitsY(VerticalStrategy.TopOutside, anchor, height, safeArea)) VerticalStrategy.TopOutside else verticalStrategy
            VerticalStrategy.TopOutside -> if (checkFitsY(VerticalStrategy.BottomOutside, anchor, height, safeArea)) VerticalStrategy.BottomOutside else verticalStrategy
            else -> verticalStrategy
        }
    }

    private fun checkFitsX(strat: HorizontalStrategy, anchor: IntRect, width: Int, safeArea: IntRect): Boolean {
        val l = calculateBaseX(strat, anchor, width) + contentOffset.x
        return l >= safeArea.left && (l + width) <= safeArea.right
    }

    private fun checkFitsY(strat: VerticalStrategy, anchor: IntRect, height: Int, safeArea: IntRect): Boolean {
        val t = calculateBaseY(strat, anchor, height) + contentOffset.y
        return t >= safeArea.top && (t + height) <= safeArea.bottom
    }

    private fun calculateBaseX(strategy: HorizontalStrategy, anchor: IntRect, width: Int): Int {
        val isLtr = layoutDirection == LayoutDirection.Ltr
        return when (strategy) {
            HorizontalStrategy.StartOutside -> if (isLtr) anchor.left - width else anchor.right
            HorizontalStrategy.EndOutside -> if (isLtr) anchor.right else anchor.left - width
            HorizontalStrategy.StartAligned -> if (isLtr) anchor.left else anchor.right - width
            HorizontalStrategy.EndAligned -> if (isLtr) anchor.right - width else anchor.left
            HorizontalStrategy.CenterAligned -> anchor.left + (anchor.width / 2) - (width / 2)
        }
    }

    private fun calculateBaseY(strategy: VerticalStrategy, anchor: IntRect, height: Int): Int {
        return when (strategy) {
            VerticalStrategy.TopOutside -> anchor.top - height
            VerticalStrategy.BottomOutside -> anchor.bottom
            VerticalStrategy.TopAligned -> anchor.top
            VerticalStrategy.BottomAligned -> anchor.bottom - height
            VerticalStrategy.CenterAligned -> anchor.top + (anchor.height / 2) - (height / 2)
        }
    }
}

// --- Positioning Strategies ---

enum class HorizontalStrategy {
    /**
     * Positioned outside the anchor, to the start side.
     */
    StartOutside,

    /**
     * Positioned outside the anchor, to the end side.
     */
    EndOutside,

    /**
     * Positioned inside the anchor, aligned to the start edge.
     */
    StartAligned,

    /**
     * Positioned inside the anchor, aligned to the center.
     */
    CenterAligned,

    /**
     * Positioned inside the anchor, aligned to the end edge.
     */
    EndAligned
}

enum class VerticalStrategy {
    /**
     * Positioned above the anchor.
     */
    TopOutside,

    /**
     * Positioned below the anchor.
     */
    BottomOutside,

    /**
     * Positioned inside the anchor, aligned to the top edge.
     */
    TopAligned,

    /**
     * Positioned inside the anchor, aligned to the center.
     */
    CenterAligned,

    /**
     * Positioned inside the anchor, aligned to the bottom edge.
     */
    BottomAligned
}

// --- Easings ---

private val ClassicOvershootEasing = Easing { fraction ->
    val tension = 2.0f
    val t = fraction - 1.0f
    t * t * ((tension + 1) * t + tension) + 1.0f
}

private val AccelerateEasing = Easing { fraction ->
    fraction * fraction
}