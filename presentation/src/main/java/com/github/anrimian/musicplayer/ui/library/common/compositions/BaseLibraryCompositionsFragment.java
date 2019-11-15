package com.github.anrimian.musicplayer.ui.library.common.compositions;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.MenuRes;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.editor.CompositionEditorActivity;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;

import static com.github.anrimian.musicplayer.Constants.Arguments.POSITION_ARG;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.shareFile;

public abstract class BaseLibraryCompositionsFragment extends LibraryFragment {

    protected abstract BaseLibraryCompositionsPresenter getBasePresenter();

    protected void onCompositionActionSelected(Composition composition,
                                             @MenuRes int menuItemId,
                                             Bundle extra) {
        switch (menuItemId) {
            case R.id.menu_play: {
                getBasePresenter().onPlayActionSelected(extra.getInt(POSITION_ARG));
                break;
            }
            case R.id.menu_play_next: {
                getBasePresenter().onPlayNextCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_queue: {
                getBasePresenter().onAddToQueueCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_playlist: {
                getBasePresenter().onAddToPlayListButtonClicked(composition);
                break;
            }
            case R.id.menu_edit: {
                startActivity(CompositionEditorActivity.newIntent(requireContext(), composition.getId()));
                break;
            }
            case R.id.menu_share: {
                shareFile(requireContext(), composition.getFilePath());
                break;
            }
            case R.id.menu_delete: {
                getBasePresenter().onDeleteCompositionButtonClicked(composition);
                break;
            }
        }
    }

    protected boolean onActionModeItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_play: {
                getBasePresenter().onPlayAllSelectedClicked();
                return true;
            }
            case R.id.menu_select_all: {
                getBasePresenter().onSelectAllButtonClicked();
                return true;
            }
            case R.id.menu_play_next: {
                getBasePresenter().onPlayNextSelectedCompositionsClicked();
                return true;
            }
            case R.id.menu_add_to_queue: {
                getBasePresenter().onAddToQueueSelectedCompositionsClicked();
                return true;
            }
            case R.id.menu_add_to_playlist: {
                getBasePresenter().onAddSelectedCompositionToPlayListClicked();
                return true;
            }
            case R.id.menu_share: {
                getBasePresenter().onShareSelectedCompositionsClicked();
                return true;
            }
            case R.id.menu_delete: {
                getBasePresenter().onDeleteSelectedCompositionButtonClicked();
                return true;
            }
        }
        return false;
    }
}
