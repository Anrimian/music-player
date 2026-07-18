package com.github.anrimian.musicplayer.ui.playlists.list

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.parcelize.Parcelize

@Immutable
data class PlaylistsState(
    val playlists: StatedData<ImmutableList<Playlist>> = StatedData.Empty(),
    val menuItems: ImmutableList<AppMenuItem> = persistentListOf(),
    val selectedPlaylists: PersistentSet<Long> = persistentSetOf(),
    val selectionModeState: SelectionModeState? = null,
    val searchQuery: String? = null
)

data class SelectionModeState(
    val selectedItemsCount: Int,
    val totalSelectedCompositionsCount: Int
)

sealed interface PlaylistsDialogs : AppDialog {
    @Parcelize
    data class OverwritePlaylistDialog(val filePayload: String) : PlaylistsDialogs
    @Parcelize
    data class NotCompletelyImportedPlaylistDialog(
        val playlistId: Long,
        val notFoundFilesCount: Int
    ): PlaylistsDialogs
}

sealed interface PlaylistsEffect : BaseEffect {
    class ShowSelectPlayListDialog(
        val playlistIds: LongArray,
        val closeSelectionMode: Boolean
    ) : PlaylistsEffect
    object LaunchPickFolder : PlaylistsEffect
}