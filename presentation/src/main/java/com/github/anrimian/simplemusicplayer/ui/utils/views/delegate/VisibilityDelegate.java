package com.github.anrimian.simplemusicplayer.ui.utils.views.delegate;

import android.view.View;

import static android.support.v4.view.ViewCompat.isLaidOut;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created on 21.01.2018.
 */

public class VisibilityDelegate implements BottomSheetDelegate {

    private View view;

    public VisibilityDelegate(View view) {
        this.view = view;
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
        view.setVisibility(slideOffset == 0 ? INVISIBLE : VISIBLE);
        view.setAlpha(slideOffset);
    }
}
