package com.github.anrimian.musicplayer.ui.player_screen.view.slide;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.isLaidOut;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

import android.animation.ArgbEvaluator;

import androidx.appcompat.widget.ActionMenuView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;

/**
 * Created on 21.01.2018.
 */

public class ToolbarVisibilityDelegate implements SlideDelegate {

    private final AdvancedToolbar toolbar;

    private ActionMenuView actionMenuView;

    public ToolbarVisibilityDelegate(AdvancedToolbar toolbar) {
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
        if (toolbar.isInActionMode()) {
            int startColor = getColorFromAttr(toolbar.getContext(), R.attr.colorPrimary);
            int endColor = getColorFromAttr(toolbar.getContext(), R.attr.actionModeBackgroundColor);
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            int color = (int) argbEvaluator.evaluate(slideOffset, startColor, endColor);
            toolbar.setBackgroundColor(color);
        } else if (!toolbar.isInSearchMode()) {
            int contentVisibility = slideOffset == 0 ? INVISIBLE : VISIBLE;
            actionMenuView.setVisibility(contentVisibility);
            actionMenuView.setAlpha(slideOffset);
            toolbar.setContentAlpha(slideOffset);
        } else {
            toolbar.setContentVisible(slideOffset == 1f);
        }
    }
}
