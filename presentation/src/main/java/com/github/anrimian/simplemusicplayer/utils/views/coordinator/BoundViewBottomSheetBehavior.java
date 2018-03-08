package com.github.anrimian.simplemusicplayer.utils.views.coordinator;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import static com.github.anrimian.simplemusicplayer.utils.java.ValueFilter.filter;

/**
 * Created on 25.02.2018.
 */

public class BoundViewBottomSheetBehavior extends LinkedBottomSheetBehavior{

    private static final float UNDEFINED = Float.MAX_VALUE;

    private float childStartY = UNDEFINED;

    private boolean heightFixed = false;

    public BoundViewBottomSheetBehavior() {
    }

    public BoundViewBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDependentViewChanged(CoordinatorLayout parent,
                                          View child,
                                          View dependency,
                                          float slideOffset) {
        int parentHeight = parent.getHeight();
        int sheetHeight = dependency.getHeight();

        if (!heightFixed && parentHeight != 0 && sheetHeight != 0) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            params.height = parentHeight - sheetHeight;
            child.setLayoutParams(params);
            heightFixed = true;
        }

        if (childStartY == UNDEFINED) {
            childStartY = child.getY();
        }

//        int childHeight = child.getMeasuredHeight();
        float childY = childStartY + (parent.getMeasuredHeight() * (1 - slideOffset));
        child.setY(childY);

        slideOffset = filter(slideOffset, 0.5f, 1f);
        child.setAlpha(slideOffset);
    }

}
