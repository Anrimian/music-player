package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.utils.compose.normalizeContentWindowInsets

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth = with(density) { windowInfo.containerSize.width.toDp() }
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }
    val isWideScreen = screenWidth > 600.dp

    Box(
        // Check and remove layout block after PlayerScreen compose migration.
        //  This layout modifier is a defensive workaround for View-to-Compose interoperability on Android 15.
        //  When Compose is hosted inside XML Views (like RecyclerView or ViewPager2), parents sometimes
        //  measure the ComposeView with unbounded/infinite constraints (Int.MAX_VALUE). This causes 
        //  the internal SubcomposeLayout in Scaffold to crash with a "Size out of range" exception 
        //  since Compose 1.7+ restricts dimension packing in its inline classes to 16,777,215.
        modifier = modifier.layout { measurable, constraints ->
            val maxAllowed = 16777215

            // An unbounded axis (Constraints.Infinity) cannot be laid out by Scaffold's
            // internal SubcomposeLayout, so resolve it to the container (screen) size.
            // containerSize is always non-zero on a real device; coerceAtLeast(1) guards
            // against Compose Preview where containerSize defaults to 0.
            val fallbackWidth = windowInfo.containerSize.width.coerceAtLeast(1)
            val fallbackHeight = windowInfo.containerSize.height.coerceAtLeast(1)

            val maxWidth = (if (constraints.hasBoundedWidth) constraints.maxWidth else fallbackWidth)
                .coerceAtMost(maxAllowed)
            val maxHeight = (if (constraints.hasBoundedHeight) constraints.maxHeight else fallbackHeight)
                .coerceAtMost(maxAllowed)
            val minWidth = constraints.minWidth.coerceAtMost(maxWidth)
            val minHeight = constraints.minHeight.coerceAtMost(maxHeight)

            val safeConstraints = Constraints(minWidth, maxWidth, minHeight, maxHeight)
            val placeable = measurable.measure(safeConstraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = if (isWideScreen) {
                {}
            } else {
                snackbarHost
            },
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentColor = contentColor,
            contentWindowInsets = normalizeContentWindowInsets(contentWindowInsets),
            content = content
        )

        if (isWideScreen) {
            val isLargeScreen = screenHeight > 480.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(contentWindowInsets),
                contentAlignment = if (isLargeScreen) Alignment.BottomStart else Alignment.BottomCenter
            ) {
                val maxSnackbarWidth = if (isLargeScreen) {
                    screenWidth - (Dimens.fabSize + (Dimens.contentHorizontalMargin * 3))
                } else {
                    600.dp
                }

                Box(modifier = Modifier.widthIn(max = maxSnackbarWidth)) {
                    snackbarHost()
                }
            }
        }
    }
}