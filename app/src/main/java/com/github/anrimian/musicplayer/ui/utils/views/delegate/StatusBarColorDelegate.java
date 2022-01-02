package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.animation.ArgbEvaluator;
import android.os.Build;
import android.util.Log;
import android.view.Window;

import androidx.annotation.ColorInt;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setStatusBarColor;

/**
 * Created on 21.01.2018.
 */

public class StatusBarColorDelegate implements SlideDelegate {

    private final int startColor;
    private final int endColor;

    private final Window window;

    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    public StatusBarColorDelegate(Window window,
                                  @ColorInt int startColor,
                                  @ColorInt int endColor) {
        this.window = window;
        this.startColor = startColor;
        this.endColor = endColor;
    }

    @Override
    public void onSlide(float slideOffset) {
        moveView(slideOffset);
    }

    private void moveView(float slideOffset) {
        int resultColor = (int) argbEvaluator.evaluate(slideOffset, startColor, endColor);
        setStatusBarColor(window, resultColor);
    }
}
