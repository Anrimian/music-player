package com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.LocalDeviceCapabilities

@Composable
fun SwipeableContentWrapper(
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (LocalDeviceCapabilities.current.isHardwareAcceleratedClippingSupported) {
        Box(
            modifier = modifier.graphicsLayer {
                shape = RoundedCornerShape(
                    topEnd = cornerRadius,
                    bottomEnd = cornerRadius
                )
                clip = cornerRadius > 0.dp
            }
        ) {
            content()
        }
    } else {
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(
                topEnd = cornerRadius,
                bottomEnd = cornerRadius
            ),
            modifier = modifier
        ) {
            content()
        }
    }
}