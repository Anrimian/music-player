package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;

import static androidx.core.view.ViewCompat.isLaidOut;

public class MoveYDelegate implements SlideDelegate {

    private final View view;
    private final float movePercent;
    private final int minCollapseHeight;

    public MoveYDelegate(View view, float movePercent) {
        this(view, movePercent, 0);
    }

    public MoveYDelegate(View view, float movePercent, int minCollapseHeight) {
        this.view = view;
        this.movePercent = movePercent;
        this.minCollapseHeight = minCollapseHeight;
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
        int viewHeight = view.getMeasuredHeight();
        float usedViewHeight = viewHeight * movePercent;
        if (minCollapseHeight != 0 && viewHeight - usedViewHeight > minCollapseHeight) {
            usedViewHeight = viewHeight - minCollapseHeight;
        }
        float childY = usedViewHeight * (1 - slideOffset);
        view.setTranslationY(childY);
    }
}
