package com.github.anrimian.musicplayer.ui.common.compose.components.fsync

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.compose.components.progress.ProgressStateIcon
import com.github.anrimian.musicplayer.ui.common.models.fsync.UiFileSyncState

@Composable
fun FileSyncStateIcon(
    fileSyncState: UiFileSyncState?,
    modifier: Modifier = Modifier,
    isFileRemote: Boolean = false,
    iconSize: Dp = 18.dp,
    offset: Dp = 3.dp
) {
    val isVisible = fileSyncState != null || isFileRemote
    val iconRes = when {
        fileSyncState != null -> if (fileSyncState.isDownloading) {
            R.drawable.ic_download
        } else {
            R.drawable.ic_upload
        }
        isFileRemote -> R.drawable.ic_cloud
        else -> 0
    }
    val showProgress = fileSyncState != null
    val progress = fileSyncState?.progress ?: -1f

    val lastIconRes = remember { mutableIntStateOf(0) }
    SideEffect {
        if (iconRes != 0) {
            lastIconRes.intValue = iconRes
        }
    }
    val currentIconRes = if (iconRes != 0) iconRes else lastIconRes.intValue

    ProgressStateIcon(
        iconRes = currentIconRes,
        progress = progress,
        showProgress = showProgress,
        isVisible = isVisible,
        modifier = modifier
            .offset(x = offset, y = offset)
            .size(iconSize)
    )
}