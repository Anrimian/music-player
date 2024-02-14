package com.github.anrimian.musicplayer.ui.library.common.library

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.dialogs.showPlaylistDuplicateEntryDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import moxy.MvpAppCompatFragment

abstract class BaseLibraryFragment : MvpAppCompatFragment(), BaseLibraryView {

    protected abstract fun getLibraryPresenter(): BaseLibraryPresenter<*>

    protected abstract fun getCoordinatorLayout(): CoordinatorLayout

    final override fun onCompositionsAddedToPlayNext(compositions: List<Composition>) {
        val message = MessagesUtils.getPlayNextMessage(requireContext(), compositions)
        getCoordinatorLayout().showSnackbar(message)
    }

    final override fun onCompositionsAddedToQueue(compositions: List<Composition>) {
        val message = MessagesUtils.getAddedToQueueMessage(requireContext(), compositions)
        getCoordinatorLayout().showSnackbar(message)
    }

    final override fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>) {
        val text = MessagesUtils.getAddToPlayListCompleteMessage(requireActivity(), playList, compositions)
        getCoordinatorLayout().showSnackbar(text)
    }

    final override fun showPlaylistDuplicateEntryDialog(
        compositions: Collection<Composition>,
        hasNonDuplicates: Boolean,
        playList: PlayList,
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
        getCoordinatorLayout().showSnackbar(errorCommand.message)
    }

}