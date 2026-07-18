package com.github.anrimian.musicplayer.ui.common.compose.components.popup

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.github.anrimian.musicplayer.ui.utils.fractionInRange
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AppPopupPagerWindow(
    primaryContent: @Composable (onNextPage: () -> Unit) -> Unit,
    secondaryContent: @Composable (onBack: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    secondaryContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    sizeTransitionStart: Float = 0.05f,
    sizeTransitionEnd: Float = 0.85f,
    dimTransitionEnd: Float = 0.6f,
    maxDimAlpha: Float = 0.4f,
    snapThreshold: Float = 0.25f,
    shadowWidth: Dp = 1.dp
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val clipShape = remember { MutableSizeShape() }

    SubcomposeLayout(modifier = modifier) { constraints ->

        // 1. Prepare Constraints
        // We fully trust the width constraints provided by the parent
        val menuContentConstraints = constraints.copy(
            minHeight = 0,
            maxWidth = constraints.maxWidth,
            maxHeight = constraints.maxHeight
        )

        // 2. Measure Primary Page
        val primaryPlaceables = subcompose("primary") {
            primaryContent {}
        }.map { placeable ->
            placeable.measure(menuContentConstraints)
        }

        val primaryWidth = primaryPlaceables.maxOfOrNull { p -> p.width } ?: 0
        val primaryHeight = primaryPlaceables.maxOfOrNull { p -> p.height } ?: 0

        // 3. Measure Secondary Page
        val secondaryPlaceables = subcompose("secondary") {
            secondaryContent {}
        }.map { placeable ->
            placeable.measure(menuContentConstraints)
        }

        val hasSecContent = secondaryPlaceables.isNotEmpty()
        val secondaryWidth = if (hasSecContent) secondaryPlaceables.maxOf { p -> p.width } else primaryWidth
        val secondaryHeight = if (hasSecContent) secondaryPlaceables.maxOf { p -> p.height } else primaryHeight

        // 4. Calculate Animation State
        val page = pagerState.currentPage
        val offset = pagerState.currentPageOffsetFraction

        // globalProgress: 0f (Page 1) -> 1f (Page 2)
        val globalProgress = when (page) {
            0 -> offset.coerceAtLeast(0f)
            1 -> 1f + offset.coerceAtMost(0f)
            else -> 0f
        }

        // Size animation happens within the defined start/end fraction
        val sizeProgress = globalProgress.fractionInRange(sizeTransitionStart, sizeTransitionEnd)

        val targetWidth = lerp(primaryWidth.toFloat(), secondaryWidth.toFloat(), sizeProgress).roundToInt()
        val targetHeight = lerp(primaryHeight.toFloat(), secondaryHeight.toFloat(), sizeProgress).roundToInt()

        // Prepare dimensions for layout
        val maxPageWidth = max(primaryWidth, secondaryWidth)
        val maxPageWidthDp = with(density) { maxPageWidth.toDp() }
        val secondaryWidthDp = with(density) { secondaryWidth.toDp() }
        val targetHeightDp = with(density) { targetHeight.toDp() }

        // 5. Final Layout
        layout(targetWidth, targetHeight) {
            val pagerPlaceables = subcompose("pager") {
                val customFlingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapPositionalThreshold = snapThreshold
                )

                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = hasSecContent,
                    verticalAlignment = Alignment.Top,
                    flingBehavior = customFlingBehavior,
                    modifier = Modifier.requiredWidth(maxPageWidthDp)
                ) { pageIndex ->
                    if (pageIndex == 0) {
                        // --- PAGE 1 ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(targetHeightDp)
                        ) {
                            Box(modifier = Modifier.align(Alignment.TopStart)) {
                                primaryContent {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            1,
                                            animationSpec = PagerAnimationSpec
                                        )
                                    }
                                }
                            }

                            // Dimming Overlay
                            val dimAlpha = globalProgress.fractionInRange(0f, dimTransitionEnd) * maxDimAlpha
                            if (dimAlpha > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = dimAlpha))
                                )
                            }
                        }
                    } else {
                        // --- PAGE 2 ---
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(secondaryWidthDp)
                                    .background(secondaryContainerColor)
                                    .drawWithContent {
                                        drawContent()
                                        // Divider/Shadow on the left side
                                        val shadowPx = shadowWidth.toPx()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.15f)
                                                ),
                                                startX = -shadowPx,
                                                endX = 0f
                                            ),
                                            topLeft = Offset(-shadowPx, 0f),
                                            size = Size(shadowPx, size.height)
                                        )
                                    }
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    secondaryContent {
                                        scope.launch {
                                            pagerState.animateScrollToPage(
                                                0,
                                                animationSpec = PagerAnimationSpec
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }.map { placeable ->
                // Measure Pager with fixed max width and constraint height
                placeable.measure(Constraints(
                    minWidth = maxPageWidth,
                    maxWidth = maxPageWidth,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight
                ))
            }

            clipShape.update(
                width = targetWidth.toFloat(),
                height = targetHeight.toFloat(),
                radius = cornerRadius.toPx()
            )

            // Place Pager with clipping
            pagerPlaceables.forEach { placeable ->
                placeable.placeWithLayer(0, 0) {
                    clip = true
                    shape = clipShape
                }
            }
        }
    }
}

private val PagerAnimationSpec = tween<Float>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

private class MutableSizeShape : Shape {
    private var width: Float = 0f
    private var height: Float = 0f
    private var radius: Float = 0f

    fun update(width: Float, height: Float, radius: Float) {
        this.width = width
        this.height = height
        this.radius = radius
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Rounded(
            RoundRect(
                left = 0f,
                top = 0f,
                right = width,
                bottom = height,
                cornerRadius = CornerRadius(radius)
            )
        )
    }
}