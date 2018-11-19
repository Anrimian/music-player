package com.github.anrimian.musicplayer.ui.utils.views.coordinator;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created on 25.02.2018.
 */

public class BoundViewBottomSheetBehavior extends LinkedBottomSheetBehavior{

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
    }
}
