package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.animation.ArgbEvaluator;
import android.view.Window;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;

import static androidx.core.content.ContextCompat.getColor;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setStatusBarColor;

public class ToolbarDelegate implements SlideDelegate {

    private final int startStatusBarColor;
    private final int endStatusBarColor;

    private final FragmentNavigation navigation;
    private final AdvancedToolbar toolbar;
    private final Window window;

    public ToolbarDelegate(FragmentNavigation navigation,
                           AdvancedToolbar toolbar,
                           Window window) {
        this.navigation = navigation;
        this.toolbar = toolbar;
        this.window = window;

        startStatusBarColor = getColor(toolbar.getContext(), R.color.selectionStatusBarColor);
        endStatusBarColor = getColorFromAttr(toolbar.getContext(), android.R.attr.statusBarColor);
    }

    @Override
    public void onSlide(float slideOffset) {
        if (!(navigation.getScreensCount() > 1
                || toolbar.isInSearchMode()
                || toolbar.isInActionMode())) {
            toolbar.setControlButtonProgress(slideOffset);
        }
        if (toolbar.isInActionMode()) {
            int startColor = getColorFromAttr(toolbar.getContext(), R.attr.actionModeTextColor);
            int endColor = getColorFromAttr(toolbar.getContext(), android.R.attr.textColorPrimaryInverse);
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            int color = (int) argbEvaluator.evaluate(slideOffset, startColor, endColor);
            toolbar.setControlButtonColor(color);

            int statusBarColor = (int) argbEvaluator.evaluate(slideOffset, startStatusBarColor, endStatusBarColor);
            setStatusBarColor(window, statusBarColor);
        }
    }
}
