package com.github.anrimian.musicplayer.ui.utils.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun LazyListState.scrollToPosition(scope: CoroutineScope, position: ListPosition) {
    if (position.position < 0) {
        return
    }
    scope.launch {
        snapshotFlow { layoutInfo.totalItemsCount }
            .filter { itemsCount -> itemsCount > 0 }
            .first()
        scrollToItem(position.position, -position.offset)
    }
}

fun LazyListState.scrollToPosition(scope: CoroutineScope, index: Int) {
    scope.launch {
        snapshotFlow { layoutInfo.totalItemsCount }
            .filter { itemsCount -> itemsCount > 0 }
            .first()
        scrollToItem(index, 0)
    }
}

@Composable
fun LazyListState.attachStopCallback(
    onStop: (ListPosition) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnStop by rememberUpdatedState(onStop)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val index = firstVisibleItemIndex
                val offset = firstVisibleItemScrollOffset
                currentOnStop(ListPosition(index, -offset))
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}