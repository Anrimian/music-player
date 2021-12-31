package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;

import static androidx.core.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class MoveOutViewDelegate implements SlideDelegate {

    private static final float UNDEFINED = -1;

    private float startY = UNDEFINED;

    private final View view;

    public MoveOutViewDelegate(View view) {
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
        if (startY == UNDEFINED) {
            startY = view.getY();
        }
        int height = view.getMeasuredHeight();
        float deltaY = startY - (height * slideOffset);
        view.setY(deltaY);
        view.setAlpha(1 - slideOffset);
    }
}
