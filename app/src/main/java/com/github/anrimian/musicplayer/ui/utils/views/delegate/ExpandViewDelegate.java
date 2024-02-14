package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DimenRes;

/**
 * Created on 14.01.2018.
 */

public class ExpandViewDelegate extends ViewSlideDelegate<View> {

    private final int expandWidth;
    private final int expandHeight;

    public ExpandViewDelegate(@DimenRes int expandSize, View view) {
        this(expandSize, expandSize, view);
    }

    public ExpandViewDelegate(@DimenRes int expandWidth, @DimenRes int expandHeight, View view) {
        super(view);
        Resources resources = view.getContext().getResources();
        this.expandWidth = resources.getDimensionPixelSize(expandWidth);
        this.expandHeight = resources.getDimensionPixelSize(expandHeight);
    }

    @Override
    protected void applySlide(View view, float slideOffset) {
        int height = (int) (expandHeight * slideOffset);
        int width = (int) (expandWidth * slideOffset);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        params.width = width;

        view.setLayoutParams(params);
//        view.setAlpha(1 - slideOffset);
    }

}
