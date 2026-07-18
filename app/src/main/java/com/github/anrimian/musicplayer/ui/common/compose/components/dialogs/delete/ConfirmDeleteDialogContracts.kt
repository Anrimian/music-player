package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete

import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConfirmDeletePlaylistDialogData(
    val ids: LongArray,
    val singleName: String? = null
) : AppDialog {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfirmDeletePlaylistDialogData) return false

        if (!ids.contentEquals(other.ids)) return false
        if (singleName != other.singleName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ids.contentHashCode()
        result = 31 * result + (singleName?.hashCode() ?: 0)
        return result
    }

}