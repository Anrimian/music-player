package com.github.anrimian.simplemusicplayer.utils.views.coordinator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created on 21.10.2017.
 */

public class HidingViewWithBottomSheetBehavior extends AppBarLayout.ScrollingViewBehavior {

    private static final float NOT_DEFINED = Float.MAX_VALUE;

    private float childStartY = NOT_DEFINED;

    public HidingViewWithBottomSheetBehavior() {
    }

    public HidingViewWithBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        Log.d("behavior", "layoutDependsOn: " + dependency.getClass().getSimpleName());

        return getBottomSheetBehavior(dependency) != null;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        BottomSheetBehavior bottomSheetBehavior = getBottomSheetBehavior(dependency);
        if (bottomSheetBehavior != null) {
            float sheetY = dependency.getY();
            int peekHeight = bottomSheetBehavior.getPeekHeight();
            int sheetHeight = dependency.getHeight();
            int hideSheetHeight = sheetHeight - peekHeight;
            float slideOffset = 1 - sheetY / hideSheetHeight;

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

    @Nullable
    private BottomSheetBehavior findBottomSheetBehavior(@NonNull CoordinatorLayout coordinatorLayout) {
        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
            View child = coordinatorLayout.getChildAt(i);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();
            if (behavior instanceof BottomSheetBehavior) {
                return (BottomSheetBehavior) behavior;
            }
        }
        return null;
    }
}
