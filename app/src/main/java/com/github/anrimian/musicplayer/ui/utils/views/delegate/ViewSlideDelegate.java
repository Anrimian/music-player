package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static androidx.core.view.ViewCompat.isLaidOut;

import android.view.View;

/**
 * Created on 21.01.2018.
 */

public abstract class ViewSlideDelegate<T extends View> implements SlideDelegate {

    private final T view;

    public ViewSlideDelegate(T view) {
        this.view = view;
    }

    @Override
    public final void onSlide(float slideOffset) {
        if (isLaidOut(view)) {
            applySlide(view, slideOffset);
        } else {
            view.post(() -> applySlide(view, slideOffset));
        }
    }

    protected abstract void applySlide(T view, float slideOffset);
}
