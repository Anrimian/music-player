package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.delete.compositions

import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConfirmDeleteCompositionsDialogData(
    val ids: LongArray,
    val singleName: String? = null,
    val hasExistingFiles: Boolean = false
) : AppDialog {

    constructor(compositions: List<CompositionModel>) : this(
        ids = compositions.map { c -> c.id }.toLongArray(),
        singleName = if (compositions.size == 1) compositions[0].title else null,
        hasExistingFiles = compositions.any { c -> c.initialSource == InitialSource.LOCAL }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfirmDeleteCompositionsDialogData) return false

        if (!ids.contentEquals(other.ids)) return false
        if (singleName != other.singleName) return false
        if (hasExistingFiles != other.hasExistingFiles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ids.contentHashCode()
        result = 31 * result + (singleName?.hashCode() ?: 0)
        result = 31 * result + hasExistingFiles.hashCode()
        return result
    }

}

data class DeleteCompositionsState(
    val isConfirmDeleteDialogEnabled: Boolean? = null,
)
