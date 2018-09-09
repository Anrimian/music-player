package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;
import android.view.ViewGroup;

import static android.support.v4.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class ChangeWidthDelegate implements SlideDelegate {

    private int baseWidth = -1;

    private final float expandPercent;

    private final View view;

    public ChangeWidthDelegate(float expandPercent, View view) {
        this.expandPercent = expandPercent;
        this.view = view;
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

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;

        view.setLayoutParams(params);

        view.setTranslationX(baseWidth - width);
    }
}
