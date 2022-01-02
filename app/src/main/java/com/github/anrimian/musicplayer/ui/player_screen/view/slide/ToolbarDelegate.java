package com.github.anrimian.musicplayer.ui.player_screen.view.slide;

import android.animation.ArgbEvaluator;
import android.view.Window;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setStatusBarColor;

public class ToolbarDelegate implements SlideDelegate {

    private final int startStatusBarColor;
    private final int endStatusBarColor;

    private final AdvancedToolbar toolbar;
    private final Window window;

    public ToolbarDelegate(AdvancedToolbar toolbar,
                           Window window) {
        this.toolbar = toolbar;
        this.window = window;

        startStatusBarColor = getColorFromAttr(window.getContext(), R.attr.actionModeStatusBarColor);
        endStatusBarColor = getColorFromAttr(toolbar.getContext(), android.R.attr.statusBarColor);
    }

    @Override
    public void onSlide(float slideOffset) {
        toolbar.setControlButtonProgress(slideOffset);

        if (toolbar.isInActionMode()) {
            int startColor = getColorFromAttr(toolbar.getContext(), R.attr.actionModeTextColor);
            int endColor = getColorFromAttr(toolbar.getContext(), R.attr.toolbarTextColorPrimary);
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            int color = (int) argbEvaluator.evaluate(slideOffset, startColor, endColor);
            toolbar.setControlButtonColor(color);

            int statusBarColor = (int) argbEvaluator.evaluate(slideOffset, startStatusBarColor, endStatusBarColor);
            setStatusBarColor(window, statusBarColor);
        }
    }
}
