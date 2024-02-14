package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.view.View;

/**
 * Created on 21.01.2018.
 */

public class VisibilityDelegate extends ViewSlideDelegate<View> {

    private final int invisibleState;

    public VisibilityDelegate(View view) {
        this(view, INVISIBLE);
    }

    public VisibilityDelegate(View view, int invisibleState) {
        super(view);
        this.invisibleState = invisibleState;
    }

    @Override
    protected void applySlide(View view, float slideOffset) {
        view.setVisibility(slideOffset == 0 ? invisibleState : VISIBLE);
        view.setAlpha(slideOffset);
    }

}
