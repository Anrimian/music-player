package com.github.anrimian.musicplayer.ui.common.compose.components.fab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.LocalAppShapes

@Composable
fun AppFloatingActionButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    visible: Boolean = true,
    shape: Shape = LocalAppShapes.current.coverShape
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it * 2 }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }),
        modifier = modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }

        Box(contentAlignment = Alignment.Center) {
            FloatingActionButton(
                onClick = {},
                modifier = Modifier.size(Dimens.fabSize),
                shape = shape,
                interactionSource = interactionSource
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painter,
                    contentDescription = contentDescription
                )
            }

            Box(
                modifier = Modifier
                    .size(Dimens.fabSize)
                    .clip(shape)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
            )
        }
    }
}


@Composable
fun rememberScrollingFabVisibility(
    listState: LazyListState
): Pair<MutableState<Boolean>, NestedScrollConnection> {
    val isVisible = remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            @Suppress("SameReturnValue")
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isScrollable = listState.canScrollForward || listState.canScrollBackward
                if (!isScrollable) {
                    return Offset.Zero
                }

                if (available.y < -5f) {
                    isVisible.value = false
                }

                if (available.y > 5f) {
                    isVisible.value = true
                }
                return Offset.Zero
            }
        }
    }

    return isVisible to nestedScrollConnection
}