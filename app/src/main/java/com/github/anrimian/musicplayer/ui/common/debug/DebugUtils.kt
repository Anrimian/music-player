package com.github.anrimian.musicplayer.ui.common.debug

import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.fsync.models.state.file.FileTaskType
import com.github.anrimian.fsync.models.storage.RemoteStorageInfo
import com.github.anrimian.fsync.models.storage.StorageAccountInfo
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

fun <T : CompositionModel> createDebugFSyncStateSyncFlow(
    currentStateProvider: () -> StatedData<List<T>>
) = flow {
    val testInfo = RemoteStorageInfo(1L, 1, "test", "test", object : StorageAccountInfo {})

    var items: List<CompositionModel> = emptyList()
    while (currentCoroutineContext().isActive && items.isEmpty()) {
        val state = currentStateProvider()
        if (state is StatedData.Content) {
            items = state.data
        } else {
            delay(500)
        }
    }

    val stateCache = items.take(50).associate { item ->
        item.id to FileSyncState(FileTaskType.DOWNLOAD, testInfo)
    }

    while (currentCoroutineContext().isActive) {
        emit(emptyMap<Long, FileSyncState>())
        delay(2000)

        stateCache.values.forEach { state ->
            state.getProgress().set(-1, -1)
        }
        emit(HashMap(stateCache))
        delay(4000)

        for (progress in 0..100) {
            stateCache.forEach { (id, state) ->
                val variance = (id % 20).toInt()
                val current = (progress - variance).coerceIn(0, 100).toLong()

                state.getProgress().set(current, 100)
            }
            emit(HashMap(stateCache))
            delay(60)
        }

        emit(emptyMap())
        delay(2000)
    }
}