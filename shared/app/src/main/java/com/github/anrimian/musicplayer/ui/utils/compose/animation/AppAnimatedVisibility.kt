package com.github.anrimian.musicplayer.ui.utils.compose.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppAnimatedVerticalVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AppAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(300)
        ),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(300)
        ),
        content = content
    )
}

@Composable
fun AppAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = expandVertically() + fadeIn(),
    exit: ExitTransition = shrinkVertically() + fadeOut(),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var isReadyToAnimate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        isReadyToAnimate = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (isReadyToAnimate) enter else EnterTransition.None,
        exit = exit,
        content = content
    )
}