package com.github.anrimian.musicplayer.ui.library.common.library

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.dialogs.showPlaylistDuplicateEntryDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import moxy.MvpAppCompatFragment

abstract class BaseLibraryFragment : MvpAppCompatFragment(), BaseLibraryView {

    protected abstract fun getLibraryPresenter(): BaseLibraryPresenter<*>

    protected abstract fun getCoordinatorLayout(): CoordinatorLayout

    protected open fun getFloatingActionButton(): View? = null

    final override fun onCompositionsAddedToPlayNext(compositions: List<CompositionModel>) {
        val message = MessagesUtils.getPlayNextMessage(requireContext(), compositions)
        getCoordinatorLayout().showSnackbar(message, anchorView = getFloatingActionButton())
    }

    final override fun onCompositionsAddedToQueue(compositions: List<CompositionModel>) {
        val message = MessagesUtils.getAddedToQueueMessage(requireContext(), compositions)
        getCoordinatorLayout().showSnackbar(message, anchorView = getFloatingActionButton())
    }

    final override fun showAddingToPlaylistComplete(
        playList: Playlist,
        compositions: List<CompositionModel>,
    ) {
        val text = MessagesUtils.getAddToPlayListCompleteMessage(requireActivity(), playList, compositions)
        getCoordinatorLayout().showSnackbar(text, anchorView = getFloatingActionButton())
    }

    final override fun showPlaylistDuplicateEntryDialog(
        compositions: Collection<CompositionModel>,
        hasNonDuplicates: Boolean,
        playList: Playlist,
        isDuplicateCheckEnabled: Boolean
    ) {
        showPlaylistDuplicateEntryDialog(
            requireContext(),
            compositions,
            hasNonDuplicates,
            playList,
            isDuplicateCheckEnabled,
            getLibraryPresenter()::onAddDuplicatePlaylistEntriesConfirmed,
            getLibraryPresenter()::onPlaylistDuplicateChecked
        )
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        getCoordinatorLayout().showSnackbar(
            errorCommand.message,
            anchorView = getFloatingActionButton()
        )
    }

}