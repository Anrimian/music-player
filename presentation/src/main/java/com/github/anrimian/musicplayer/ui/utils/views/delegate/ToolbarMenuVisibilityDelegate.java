package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.support.annotation.Nullable;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import static android.support.v4.view.ViewCompat.isLaidOut;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created on 21.01.2018.
 */

public class ToolbarMenuVisibilityDelegate implements SlideDelegate {

    private final Toolbar toolbar;

    private ActionMenuView actionMenuView;

    public ToolbarMenuVisibilityDelegate(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public void onSlide(float slideOffset) {
        ActionMenuView view = getActionMenuView();
        if (view != null) {//TODO call before create options menu in toolbar
            if (isLaidOut(view)) {
                makeVisible(slideOffset);
            } else {
                view.post(() -> makeVisible(slideOffset));
            }
        }
    }

    private void makeVisible(float slideOffset) {
        actionMenuView.setVisibility(slideOffset == 0 ? INVISIBLE : VISIBLE);
        actionMenuView.setAlpha(slideOffset);
    }

    @Nullable
    private ActionMenuView getActionMenuView() {
        if (actionMenuView == null) {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof ActionMenuView) {
                    actionMenuView = (ActionMenuView) child;
                    break;
                }
            }
        }
        return actionMenuView;
    }
}
