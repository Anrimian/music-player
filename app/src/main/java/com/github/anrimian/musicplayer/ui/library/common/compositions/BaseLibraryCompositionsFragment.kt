package com.github.anrimian.musicplayer.ui.library.common.compositions

import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.showCompositionPopupMenu
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.main.MainActivity

abstract class BaseLibraryCompositionsFragment : BaseLibraryFragment() {
    
    abstract override fun getLibraryPresenter(): BaseLibraryCompositionsPresenter<*, *>

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

    protected fun onCompositionMenuClicked(view: View, position: Int, composition: Composition) {
        showCompositionPopupMenu(view, R.menu.composition_actions_menu, composition) { item ->
            onCompositionActionSelected(composition, item.itemId, position)
        }
    }

    private fun onCompositionActionSelected(
        composition: Composition,
        @MenuRes menuItemId: Int,
        position: Int
    ) {
        val act = activity ?: return
        when (menuItemId) {
            R.id.menu_play -> getLibraryPresenter().onPlayActionSelected(position)
            R.id.menu_play_next -> getLibraryPresenter().onPlayNextCompositionClicked(composition)
            R.id.menu_add_to_queue -> getLibraryPresenter().onAddToQueueCompositionClicked(composition)
            R.id.menu_add_to_playlist -> getLibraryPresenter().onAddToPlayListButtonClicked(composition)
            R.id.menu_edit -> startActivity(CompositionEditorActivity.newIntent(act, composition.id))
            R.id.menu_show_in_folders -> MainActivity.showInFolders(act, composition)
            R.id.menu_share -> shareComposition(this, composition)
            R.id.menu_delete -> getLibraryPresenter().onDeleteCompositionButtonClicked(composition)
        }
    }
}