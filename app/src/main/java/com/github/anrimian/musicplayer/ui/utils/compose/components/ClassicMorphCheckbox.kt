package com.github.anrimian.musicplayer.ui.utils.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R

@Composable
fun ClassicMorphCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    iconSize: Dp = 14.dp,
    shape: Shape = RoundedCornerShape(2.dp),
    borderWidth: Dp = 2.dp,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    checkmarkColor: Color = MaterialTheme.colorScheme.onPrimary,
    animationDuration: Int = 350
) {
    Box(
        modifier = modifier.padding(horizontal = 7.dp).size(size),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: Border
        val borderAlpha by animateFloatAsState(
            targetValue = if (checked) 0f else 1f,
            animationSpec = tween(durationMillis = animationDuration),
            label = "borderAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(borderAlpha)
                .border(borderWidth, uncheckedColor, shape)
        )

        // Layer 2: Fill
        val fillScale by animateFloatAsState(
            targetValue = if (checked) 1f else 0f,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing
            ),
            label = "fillScale"
        )

        if (fillScale > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(fillScale)
                    .background(checkedColor, shape)
            )
        }

        // Layer 3: Checkmark
        val iconDelay = (animationDuration * 0.4f).toInt()

        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(tween(animationDuration, delayMillis = iconDelay)) +
                    scaleIn(
                        initialScale = 0.5f,
                        animationSpec = tween(animationDuration, delayMillis = iconDelay)
                    ),
            exit = fadeOut(tween(animationDuration / 2))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = checkmarkColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}