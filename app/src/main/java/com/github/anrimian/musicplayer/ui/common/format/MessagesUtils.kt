package com.github.anrimian.musicplayer.ui.common.format

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName
import com.github.anrimian.musicplayer.ui.common.snackbars.AppSnackbar
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import com.github.anrimian.musicplayer.ui.utils.getDimensionPixelSize
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun ViewGroup.showSnackbar(
    @StringRes text: Int,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
    anchorView: View? = null,
    actionText: String? = null,
    action: (() -> Unit)? = null,
): AppSnackbar {
    return showSnackbar(context.getString(text), duration, anchorView, actionText, action)
}

fun ViewGroup.showSnackbar(
    text: String,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
    anchorView: View? = null,
    actionText: String? = null,
    action: (() -> Unit)? = null,
): AppSnackbar {
    val snackbar = MessagesUtils.makeSnackbar(this, text, duration)
        .also { snackbar ->
            if (anchorView != null && anchorView.translationY == 0f) {
                if (measuredWidth < context.getDimensionPixelSize(R.dimen.snackbar_gravity_width_threshold)) {
                    snackbar.setAnchorView(anchorView)
                } else {
                    //if screen width is too large - place snackbar near fab
                    val snackbarView = snackbar.view
                    snackbarView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                        gravity = Gravity.START or Gravity.BOTTOM
                    }
                    snackbarView.updatePaddingRelative(
                        start = context.getDimensionPixelSize(R.dimen.snackbar_large_margin_start),
                        end = context.getDimensionPixelSize(R.dimen.snackbar_large_margin_end),
                        bottom = context.getDimensionPixelSize(R.dimen.snackbar_large_margin_bottom),
                    )
                }
            }
            if (actionText != null) {
                snackbar.setAction(actionText, action)
            }
        }
    snackbar.show()
    return snackbar
}

fun getExportedPlaylistsMessage(context: Context, playlists: List<Playlist>): String {
    val size = playlists.size
    if (size == 1) {
        return context.getString(R.string.export_playlists_success, playlists[0].name)
    }
    return context.resources.getQuantityString(R.plurals.export_playlists_success, size, size)
}

fun getDeletedPlaylistsMessage(context: Context, playlists: Collection<Playlist>): String {
    val size = playlists.size
    if (size == 1) {
        return context.getString(R.string.play_list_deleted, playlists.first().name)
    }
    return context.resources.getQuantityString(R.plurals.delete_playlists_success, size, size)
}

fun getMissingFilesMessage(context: Context, count: Int): String {
    return context.getString(R.string.missing_files_detected) + context.getString(R.string.braces_template, count)

}

@Composable
fun getMissingFilesMessage(count: Int): String {
    return getMissingFilesMessage(LocalContext.current, count)
}

fun createAddToPlaylistMessage(
    playList: Playlist,
    compositions: List<CompositionModel>
): UiText {
    val size = compositions.size
    return if (size == 1) {
        val composition = compositions[0]
        val compositionName = formatCompositionName(composition)

        UiText.StringResource(
            R.string.add_to_playlist_success_template,
            compositionName,
            playList.name
        )
    } else {
        UiText.PluralResource(
            R.plurals.add_to_playlist_count_success_template,
            size,
            size,
            playList.name
        )
    }
}

fun createDeletePlaylistItemCompleteMessage(
    playList: Playlist,
    items: List<PlaylistEntry>
): UiText {
    val size = items.size
    return if (size == 1) {
        UiText.StringResource(
            R.string.delete_from_playlist_success_template,
            formatCompositionName(items[0]),
            playList.name
        )
    } else {
        UiText.PluralResource(
            R.plurals.delete_from_playlist_count_success_template,
            size,
            size,
            playList.name
        )
    }
}

fun createDeleteCompleteMessage(
    compositions: List<DeletedComposition>
): UiText {
    val size = compositions.size
    return if (size == 1) {
        UiText.StringResource(
            R.string.delete_composition_success,
            formatCompositionName(compositions[0])
        )
    } else {
        UiText.PluralResource(
            R.plurals.delete_compositions_success,
            size,
            size
        )
    }
}

fun createPlayNextMessage(
    compositions: List<CompositionModel>
): UiText {
    val size = compositions.size
    return if (size == 1) {
        UiText.StringResource(
            R.string.play_next_message_single,
            formatCompositionName(compositions[0])
        )
    } else {
        UiText.PluralResource(
            R.plurals.play_next_message,
            size,
            size
        )
    }
}

fun createAddedToQueueMessage(
    compositions: List<CompositionModel>
): UiText {
    val size = compositions.size
    return if (size == 1) {
        UiText.StringResource(
            R.string.added_to_queue_message_single,
            formatCompositionName(compositions[0])
        )
    } else {
        UiText.PluralResource(
            R.plurals.added_to_queue_message,
            size,
            size
        )
    }
}

fun createExportedPlaylistsMessage(playlists: List<Playlist>): UiText {
    val size = playlists.size
    return if (size == 1) {
        UiText.StringResource(
            R.string.export_playlists_success,
            playlists[0].name
        )
    } else {
        UiText.PluralResource(
            R.plurals.export_playlists_success,
            size,
            size
        )
    }
}

fun createDeletedPlaylistsMessage(ids: LongArray, singleName: String?): UiText {
    return if (singleName != null) {
        UiText.StringResource(R.string.play_list_deleted, singleName)
    } else {
        val size = ids.size
        UiText.PluralResource(
            R.plurals.delete_playlists_success,
            size,
            size
        )
    }
}