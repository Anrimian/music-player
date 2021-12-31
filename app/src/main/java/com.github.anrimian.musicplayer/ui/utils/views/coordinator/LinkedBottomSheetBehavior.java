package com.github.anrimian.musicplayer.ui.utils.views.coordinator;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created on 08.03.2018.
 */

public abstract class LinkedBottomSheetBehavior extends AppBarLayout.ScrollingViewBehavior {

    public LinkedBottomSheetBehavior() {
    }

    public LinkedBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return getBottomSheetBehavior(dependency) != null;
    }

    protected abstract void onDependentViewChanged(CoordinatorLayout parent,
                                              View child,
                                              View dependency,
                                              float slideOffset);

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        BottomSheetBehavior bottomSheetBehavior = getBottomSheetBehavior(dependency);
        if (bottomSheetBehavior != null) {
            float slideOffset = getSlideOffset(parent, dependency, bottomSheetBehavior);
            onDependentViewChanged(parent, child, dependency, slideOffset);
        }
        return true;
    }

    private float getSlideOffset(CoordinatorLayout parent, View dependency, BottomSheetBehavior bottomSheetBehavior) {
        int parentHeight = parent.getMeasuredHeight();
        float sheetY = dependency.getY();
        int peekHeight = bottomSheetBehavior.getPeekHeight();
        int sheetHeight = dependency.getHeight();
        float collapseY = parentHeight - peekHeight;
        float expandY = parentHeight - sheetHeight;
        float deltaY = collapseY - expandY;

        return (parentHeight - peekHeight - sheetY) / deltaY;
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
