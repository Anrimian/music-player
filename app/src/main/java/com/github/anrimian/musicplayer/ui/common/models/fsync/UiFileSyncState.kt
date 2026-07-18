package com.github.anrimian.musicplayer.ui.common.models.fsync

import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.fsync.models.state.file.FileTaskType
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap

data class UiFileSyncState(
    val isDownloading: Boolean,
    val progress: Float
)

fun Map<Long, FileSyncState>.toUiStateMap(): PersistentMap<Long, UiFileSyncState> {
    return mapValues { entry -> entry.value.toUiState() }.toPersistentMap()
}

fun FileSyncState.toUiState(): UiFileSyncState {
    val progress = getProgress()
    val total = progress.total
    val progressValue = if (total <= 0L) -1f else progress.current.toFloat() / total
    return UiFileSyncState(
        isDownloading = taskType == FileTaskType.DOWNLOAD,
        progress = progressValue
    )
}
