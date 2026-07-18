package com.github.anrimian.musicplayer.ui.utils.compose

import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.utils.isTabletLand

fun normalizeContentWindowInsets(
    contentWindowInsets: WindowInsets
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    contentWindowInsets
} else {
    WindowInsets(0.dp)
}

@Composable
fun Modifier.navigationBarPaddingCompat() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !LocalContext.current.isTabletLand()) {
    windowInsetsPadding(WindowInsets.navigationBars)
} else {
    this
}