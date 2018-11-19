package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import androidx.appcompat.widget.ActionMenuView;

import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;

import static androidx.core.view.ViewCompat.isLaidOut;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created on 21.01.2018.
 */

public class ToolbarMenuVisibilityDelegate implements SlideDelegate {

    private final AdvancedToolbar toolbar;

    private ActionMenuView view;

    public ToolbarMenuVisibilityDelegate(AdvancedToolbar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (view == null) {
            view = toolbar.getActionMenuView();
        }
        if (view != null) {
            if (isLaidOut(view)) {
                makeVisible(slideOffset);
            } else {
                view.post(() -> makeVisible(slideOffset));
            }
        }
    }

    private void makeVisible(float slideOffset) {
        if (!toolbar.isInSearchMode()) {
            view.setVisibility(slideOffset == 0 ? INVISIBLE : VISIBLE);
            view.setAlpha(slideOffset);
        }
    }
}
