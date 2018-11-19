package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;

import static androidx.core.view.ViewCompat.isLaidOut;

public class MoveYDelegate implements SlideDelegate {

    private final View view;
    private final float movePercent;

    public MoveYDelegate(View view, float movePercent) {
        this.view = view;
        this.movePercent = movePercent;
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
        float childY = view.getMeasuredHeight() * movePercent * (1 - slideOffset);
        view.setTranslationY(childY);
//        if (startY == UNDEFINED) {
//            startY = view.getY();
//        }
//        int height = view.getMeasuredHeight();
//        float deltaY = startY - (height * slideOffset);
//        view.setY(deltaY);
//        view.setAlpha(1 - slideOffset);
    }
}
