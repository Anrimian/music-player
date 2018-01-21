package com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet;

import android.view.View;

import static android.support.v4.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class MoveOutViewDelegate implements BottomSheetDelegate {

    private static final float UNDEFINED = -1;

    private float startY = UNDEFINED;

    private View view;

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
