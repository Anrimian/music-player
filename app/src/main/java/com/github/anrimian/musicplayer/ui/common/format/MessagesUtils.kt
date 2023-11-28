package com.github.anrimian.musicplayer.ui.common.format

import android.content.Context
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun ViewGroup.showSnackbar(
    text: String,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_SHORT,
) {
    MessagesUtils.makeSnackbar(this, text, duration).show()
}

fun getExportedPlaylistsMessage(context: Context, playlists: List<PlayList>): String {
    val size = playlists.size
    if (size == 1) {
        return context.getString(R.string.export_playlists_success, playlists[0].name)
    }
    return context.resources.getQuantityString(R.plurals.export_playlists_success, size, size)

}