package com.github.anrimian.musicplayer.ui.library.common.compositions

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.MenuRes
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.editor.composition.newCompositionEditorIntent
import com.github.anrimian.musicplayer.ui.library.LibraryFragment

abstract class BaseLibraryCompositionsFragment : LibraryFragment() {
    
    protected abstract fun getLibraryPresenter(): BaseLibraryCompositionsPresenter<*>
    
    protected fun onCompositionActionSelected(
        composition: Composition,
        @MenuRes menuItemId: Int,
        extra: Bundle
    ) {
        when (menuItemId) {
            R.id.menu_play -> getLibraryPresenter().onPlayActionSelected(extra.getInt(Constants.Arguments.POSITION_ARG))
            R.id.menu_play_next -> getLibraryPresenter().onPlayNextCompositionClicked(composition)
            R.id.menu_add_to_queue -> getLibraryPresenter().onAddToQueueCompositionClicked(composition)
            R.id.menu_add_to_playlist -> getLibraryPresenter().onAddToPlayListButtonClicked(composition)
            R.id.menu_edit -> startActivity(newCompositionEditorIntent(requireContext(), composition.id))
            R.id.menu_share -> DialogUtils.shareComposition(requireContext(), composition)
            R.id.menu_delete -> getLibraryPresenter().onDeleteCompositionButtonClicked(composition)
        }
    }

    protected fun onActionModeItemClicked(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_play -> getLibraryPresenter().onPlayAllSelectedClicked()
            R.id.menu_select_all -> getLibraryPresenter().onSelectAllButtonClicked()
            R.id.menu_play_next -> getLibraryPresenter().onPlayNextSelectedCompositionsClicked()
            R.id.menu_add_to_queue -> getLibraryPresenter().onAddToQueueSelectedCompositionsClicked()
            R.id.menu_add_to_playlist -> getLibraryPresenter().onAddSelectedCompositionToPlayListClicked()
            R.id.menu_share -> getLibraryPresenter().onShareSelectedCompositionsClicked()
            R.id.menu_delete -> getLibraryPresenter().onDeleteSelectedCompositionButtonClicked()
        }
    }
}