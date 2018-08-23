package com.github.anrimian.simplemusicplayer.ui.utils.views.delegate;

import android.content.res.Resources;
import android.support.annotation.DimenRes;
import android.view.View;
import android.view.ViewGroup;

import static android.support.v4.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class ExpandViewDelegate implements BottomSheetDelegate {

    private final int expandWidth;
    private final int expandHeight;

    private final View view;

    public ExpandViewDelegate(@DimenRes int expandSize, View view) {
        this(expandSize, expandSize, view);
    }

    public ExpandViewDelegate(@DimenRes int expandWidth, @DimenRes int expandHeight, View view) {
        Resources resources = view.getContext().getResources();
        this.expandWidth = resources.getDimensionPixelSize(expandWidth);
        this.expandHeight = resources.getDimensionPixelSize(expandHeight);
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
        int height = (int) (expandHeight * slideOffset);
        int width = (int) (expandWidth * slideOffset);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        params.width = width;

        view.setLayoutParams(params);
//        view.setAlpha(1 - slideOffset);
    }
}
