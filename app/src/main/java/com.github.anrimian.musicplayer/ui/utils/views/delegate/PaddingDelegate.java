package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.content.res.Resources;
import androidx.annotation.DimenRes;
import android.view.View;

import static androidx.core.view.ViewCompat.isLaidOut;

/**
 * Created on 13.01.2018.
 */

public class PaddingDelegate implements SlideDelegate {

    private final float startPadding;
    private final float endPadding;

    private final View view;

    public PaddingDelegate(float startPadding, float endPadding, View view) {
        this.startPadding = startPadding;
        this.endPadding = endPadding;
        this.view = view;
    }

    public PaddingDelegate(View view, @DimenRes int startResId, @DimenRes int endResId) {
        this.view = view;
        Resources resources = view.getResources();
        startPadding = resources.getDimensionPixelSize(startResId);
        endPadding = resources.getDimensionPixelSize(endResId);
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
        float deltaPadding = endPadding - startPadding;
        int resultPadding = (int) (startPadding + (deltaPadding * slideOffset));
        view.setPadding(resultPadding, resultPadding, resultPadding, resultPadding);
    }
}
