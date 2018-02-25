package com.github.anrimian.simplemusicplayer.utils.views.coordinator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created on 25.02.2018.
 */

public class BoundViewBottomSheetBehavior extends AppBarLayout.ScrollingViewBehavior {

    private static final float NOT_DEFINED = Float.MAX_VALUE;

    private float childStartY = NOT_DEFINED;

    public BoundViewBottomSheetBehavior() {
    }

    public BoundViewBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return getBottomSheetBehavior(dependency) != null;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        BottomSheetBehavior bottomSheetBehavior = getBottomSheetBehavior(dependency);
        if (bottomSheetBehavior != null) {
            int parentHeight = parent.getMeasuredHeight();
            float sheetY = dependency.getY();
            int peekHeight = bottomSheetBehavior.getPeekHeight();
            int sheetHeight = dependency.getHeight();
            float collapseY = parentHeight - peekHeight;
            float expandY = parentHeight - sheetHeight;
            float deltaY = collapseY - expandY;

            float slideOffset = 1 - (parentHeight - peekHeight - sheetY) / deltaY;

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            params.height = parentHeight - sheetHeight;
            child.setLayoutParams(params);

//            child.setAlpha(1 - slideOffset);

            if (childStartY == NOT_DEFINED) {
                childStartY = child.getY();
            }

            int childHeight = child.getMeasuredHeight();
            float childY = childStartY - (childHeight * slideOffset);
            child.setY(childY);
        }
        return true;
    }

    @Nullable
    private BottomSheetBehavior getBottomSheetBehavior(@NonNull View view) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior instanceof BottomSheetBehavior) {
            return (BottomSheetBehavior) behavior;
        }
        return null;
    }
}
