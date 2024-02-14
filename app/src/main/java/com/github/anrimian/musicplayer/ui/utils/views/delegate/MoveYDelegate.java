package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;

public class MoveYDelegate extends ViewSlideDelegate<View> {

    private final float movePercent;
    private final int minCollapseHeight;

    public MoveYDelegate(View view, float movePercent) {
        this(view, movePercent, 0);
    }

    public MoveYDelegate(View view, float movePercent, int minCollapseHeight) {
        super(view);
        this.movePercent = movePercent;
        this.minCollapseHeight = minCollapseHeight;
    }

    @Override
    protected void applySlide(View view, float slideOffset) {
        int viewHeight = view.getMeasuredHeight();
        float usedViewHeight = viewHeight * movePercent;
        if (minCollapseHeight != 0 && viewHeight - usedViewHeight > minCollapseHeight) {
            usedViewHeight = viewHeight - minCollapseHeight;
        }
        float childY = usedViewHeight * (1 - slideOffset);
        view.setTranslationY(childY);
    }

}
