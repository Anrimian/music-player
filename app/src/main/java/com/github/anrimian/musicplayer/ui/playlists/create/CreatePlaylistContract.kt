package com.github.anrimian.musicplayer.ui.playlists.create

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Parcelize
object CreatePlaylistDialogData : AppDialog

@Immutable
data class CreatePlaylistState(
    val isLoading: Boolean = false,
    val error: ErrorCommand? = null
)

sealed interface CreatePlaylistEffect : BaseEffect {
    data object Close : CreatePlaylistEffect
}
