package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static androidx.core.view.ViewCompat.isLaidOut;

import android.view.View;

import com.github.anrimian.musicplayer.ui.utils.ViewUtils;

/**
 * Created on 14.01.2018.
 */

public class MoveXDelegate implements SlideDelegate {

    private int baseWidth = -1;

    private final float expandPercent;
    private final View view;

    public MoveXDelegate(float expandPercent, View view) {
        this.view = view;
        if (ViewUtils.isRtl(view)) {
            this.expandPercent = expandPercent*-1;
        } else {
            this.expandPercent = expandPercent;
        }
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(view)) {
            moveView(slideOffset);
        } else {
            view.post(() -> moveView(slideOffset));
        }
    }

    private void moveView(float slideOffset) {
        if (baseWidth == -1) {
            baseWidth = view.getWidth();
        }
        int width = (int) (baseWidth * (1 - slideOffset * expandPercent));
        view.setTranslationX(baseWidth - width);
    }
}
