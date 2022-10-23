package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.isLaidOut;

import android.view.View;

/**
 * Created on 21.01.2018.
 */

public class VisibilityDelegate implements SlideDelegate {

    private final View view;

    private final int invisibleState;

    public VisibilityDelegate(View view) {
        this.view = view;
        invisibleState = INVISIBLE;
    }

    public VisibilityDelegate(View view, int invisibleState) {
        this.view = view;
        this.invisibleState = invisibleState;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(view)) {
            makeVisible(slideOffset);
        } else {
            view.post(() -> makeVisible(slideOffset));
        }
    }

    private void makeVisible(float slideOffset) {
        view.setVisibility(slideOffset == 0 ? invisibleState : VISIBLE);
        view.setAlpha(slideOffset);
    }
}
