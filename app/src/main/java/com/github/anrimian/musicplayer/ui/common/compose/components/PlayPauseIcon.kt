package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.R

@Composable
fun PlayPauseIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_play_to_pause)
    val painter = rememberAnimatedVectorPainter(image, atEnd = isPlaying)

    Icon(
        painter = painter,
        contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
        tint = tint,
        modifier = modifier
    )
}