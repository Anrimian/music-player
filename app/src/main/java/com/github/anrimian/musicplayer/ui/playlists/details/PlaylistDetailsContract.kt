package com.github.anrimian.musicplayer.ui.playlists.details

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.MessageAction
import com.github.anrimian.musicplayer.ui.common.models.fsync.UiFileSyncState
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Immutable
data class PlaylistDetailsState(
    val playlist: Playlist? = null,
    val playlistItems: StatedData<PersistentList<PlaylistEntry>> = StatedData.Empty(),
    val currentComposition: CurrentComposition? = null,
    val fileSyncStates: PersistentMap<Long, UiFileSyncState> = persistentMapOf(),
    val menuItems: ImmutableList<AppMenuItem> = persistentListOf(),
    val searchQuery: String? = null,
    val isRandomEnabled: Boolean = false,
    val isCoversEnabled: Boolean = true
)

data object RestoreRemovedPlaylistEntry : MessageAction
data object UndoSortAction : MessageAction

sealed interface PlaylistDetailsEffect : BaseEffect {
    class ShowSelectPlaylistDialog(val compositionIds: LongArray) : PlaylistDetailsEffect
    class ShowInFolders(val compositionId: Long) : PlaylistDetailsEffect
    object LaunchPickFolder : PlaylistDetailsEffect
}
