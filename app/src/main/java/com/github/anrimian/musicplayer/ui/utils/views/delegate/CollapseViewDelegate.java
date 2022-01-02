package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;
import android.view.ViewGroup;

import static androidx.core.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class CollapseViewDelegate implements SlideDelegate {

    private static final int UNDEFINED = -1;

    private int startHeight = UNDEFINED;
    private int startWidth = UNDEFINED;

    private final View view;

    public CollapseViewDelegate(View view) {
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
        if (startHeight == UNDEFINED) {
            startHeight = view.getHeight();
            startWidth = view.getWidth();
        }
        int height = (int) (startHeight - (startHeight * slideOffset));
        int width = (int) (startWidth - (startWidth * slideOffset));
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        params.width = width;
        view.setLayoutParams(params);
    }
}
