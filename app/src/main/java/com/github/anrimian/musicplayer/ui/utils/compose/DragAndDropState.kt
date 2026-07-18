package com.github.anrimian.musicplayer.ui.utils.compose

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class DragAndDropState(
    private val onMove: (Int, Int) -> Unit,
    private val onDragEnd: (Int, Int) -> Unit
) {
    private var dragStartIndex by mutableStateOf<Int?>(null)
    private var dragCurrentIndex by mutableStateOf<Int?>(null)

    fun onMove(from: LazyListItemInfo, to: LazyListItemInfo) {
        if (dragStartIndex == null) {
            dragStartIndex = from.index
        }
        dragCurrentIndex = to.index
        
        onMove(from.index, to.index)
    }

    fun onDragStateChanged(isDragging: Boolean) {
        if (!isDragging) {
            val start = dragStartIndex
            val end = dragCurrentIndex

            if (start != null && end != null && start != end) {
                onDragEnd(start, end)
            }

            dragStartIndex = null
            dragCurrentIndex = null
        }
    }
}

@Composable
fun rememberDragDropState(
    onMove: (Int, Int) -> Unit,
    onDragEnd: (Int, Int) -> Unit
): DragAndDropState {
    return remember(onMove, onDragEnd) {
        DragAndDropState(onMove, onDragEnd)
    }
}