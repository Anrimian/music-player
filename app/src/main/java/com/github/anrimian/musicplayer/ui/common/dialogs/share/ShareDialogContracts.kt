package com.github.anrimian.musicplayer.ui.common.dialogs.share

import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShareDialogData(
    val ids: LongArray,
    val hasMissingFiles: Boolean
) : AppDialog {

    constructor(compositions: Collection<CompositionModel>) : this(
        ids = compositions.map { c -> c.id }.toLongArray(),
        hasMissingFiles = compositions.any { !it.isFileExists }
    )

    constructor(composition: CompositionModel) : this(
        ids = longArrayOf(composition.id),
        hasMissingFiles = !composition.isFileExists
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShareDialogData

        if (!ids.contentEquals(other.ids)) return false
        if (hasMissingFiles != other.hasMissingFiles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ids.contentHashCode()
        result = 31 * result + hasMissingFiles.hashCode()
        return result
    }
}

data class ShareState(
    val isInLoadingMode: Boolean = false,
    val progress: Int = 0,
    val prepared: Int = 0,
    val total: Int = 0,
    val error: ErrorCommand? = null
)

sealed interface ShareEffect : BaseEffect {
    data object Close : ShareEffect
    data class Share(val sources: ArrayList<CompositionContentSource>) : ShareEffect
    data class Error(val error: ErrorCommand) : ShareEffect
}