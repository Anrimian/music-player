package com.github.anrimian.musicplayer.ui.common.mvvm.progress

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.ui.utils.compose.UiText

@Immutable
sealed interface StatedData<out T> {
    val data: T? get() = null

    data object Loading : StatedData<Nothing>
    data class Content<T>(override val data: T) : StatedData<T>
    data class Error(val message: String) : StatedData<Nothing>
    data class Empty(val message: UiText? = null) : StatedData<Nothing>
}

fun <L : List<T>, T> L.toStatedData(
    emptyMessage: UiText? = null
): StatedData<L> {
    return if (isEmpty()) {
        StatedData.Empty(emptyMessage)
    } else {
        StatedData.Content(this)
    }
}

fun <L : List<T>, T> L.toStatedData(
    @StringRes emptyMessageId: Int
): StatedData<L> = toStatedData(UiText.StringResource(emptyMessageId))