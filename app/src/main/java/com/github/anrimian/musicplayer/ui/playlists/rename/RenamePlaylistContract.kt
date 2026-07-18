package com.github.anrimian.musicplayer.ui.playlists.rename

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Parcelize
data class RenamePlaylistDialogData(
    val playlistId: Long,
    val initialName: String
) : AppDialog

@Immutable
data class RenamePlaylistState(
    val isLoading: Boolean = false,
    val error: ErrorCommand? = null
)

sealed interface RenamePlaylistEffect : BaseEffect {
    data object Close : RenamePlaylistEffect
}