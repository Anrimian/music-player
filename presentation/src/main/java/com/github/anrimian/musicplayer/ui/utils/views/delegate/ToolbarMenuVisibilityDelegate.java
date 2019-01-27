package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.animation.ArgbEvaluator;

import androidx.appcompat.widget.ActionMenuView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;

import static androidx.core.view.ViewCompat.isLaidOut;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 21.01.2018.
 */

public class ToolbarMenuVisibilityDelegate implements SlideDelegate {

    private final AdvancedToolbar toolbar;

    private ActionMenuView actionMenuView;

    public ToolbarMenuVisibilityDelegate(AdvancedToolbar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (actionMenuView == null) {
            actionMenuView = toolbar.getActionMenuView();
        }
        if (actionMenuView != null) {
            if (isLaidOut(actionMenuView)) {
                makeVisible(slideOffset);
            } else {
                actionMenuView.post(() -> makeVisible(slideOffset));
            }
        }
    }

    private void makeVisible(float slideOffset) {
        if (toolbar.isInSelectionMode()) {
            int startColor = getColorFromAttr(toolbar.getContext(), R.attr.colorPrimary);
            int endColor = getColorFromAttr(toolbar.getContext(), android.R.attr.windowBackground);
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            int color = (int) argbEvaluator.evaluate(slideOffset, startColor, endColor);
            toolbar.setBackgroundColor(color);
        } else if (!toolbar.isInSearchMode()) {
            actionMenuView.setVisibility(slideOffset == 0 ? INVISIBLE : VISIBLE);
            actionMenuView.setAlpha(slideOffset);
        }
    }
}
