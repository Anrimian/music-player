package com.github.anrimian.musicplayer.ui.utils.compose.dialogs

import android.os.Parcelable
import androidx.compose.runtime.Composable

@Composable
fun <D : Parcelable> DialogStack(
    dialogStack: List<D>,
    onDismissRequest: () -> Unit,
    render: @Composable (D) -> Unit
) {
    dialogStack.forEach { dialog ->
        render(dialog)
    }
}