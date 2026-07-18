package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Check and remove after full compose migration.
 * Case: scroll lyrics out of highlight, trigger config change
 */
@Composable
fun PlayerScreenScaffold(
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    var isReady by remember { mutableStateOf(isPreview) }

    if (!isPreview) {
        LaunchedEffect(Unit) {
            withFrameNanos { }
            isReady = true
        }
    }

    if (isReady) {
        AppScaffold(
            modifier = modifier,
            snackbarHost = snackbarHost,
        ) {
            content()
        }
    } else {
        Box(modifier = modifier)
    }
}