package com.github.anrimian.musicplayer.ui.utils.views.coordinator;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created on 21.10.2017.
 */

public class HidingViewWithBottomSheetBehavior extends LinkedBottomSheetBehavior {

    private static final float UNDEFINED = Float.MAX_VALUE;

    private float childStartY = UNDEFINED;

    public HidingViewWithBottomSheetBehavior() {
    }

    public HidingViewWithBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDependentViewChanged(CoordinatorLayout parent,
                                          View child,
                                          View dependency,
                                          float slideOffset) {
        if (childStartY == UNDEFINED) {
            childStartY = child.getY();
        }

        int childHeight = child.getMeasuredHeight();
        float childY = childStartY - (childHeight * slideOffset);
        child.setY(childY);
    }
}
