package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;

import com.github.anrimian.musicplayer.ui.utils.ViewUtils;

/**
 * Created on 14.01.2018.
 */

public class MoveXDelegate extends ViewSlideDelegate<View> {

    private int baseWidth = -1;

    private final float expandPercent;

    public MoveXDelegate(float expandPercent, View view) {
        super(view);
        if (ViewUtils.isRtl(view)) {
            this.expandPercent = expandPercent*-1;
        } else {
            this.expandPercent = expandPercent;
        }
    }

    @Override
    protected void applySlide(View view, float slideOffset) {
        if (baseWidth == -1) {
            baseWidth = view.getWidth();
        }
        int width = (int) (baseWidth * (1 - slideOffset * expandPercent));
        view.setTranslationX(baseWidth - width);
    }

}
