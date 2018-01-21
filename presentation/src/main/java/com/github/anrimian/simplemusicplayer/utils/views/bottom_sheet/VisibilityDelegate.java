package com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet;

import android.view.View;

import static android.support.v4.view.ViewCompat.isLaidOut;

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
        view.setAlpha(slideOffset);
    }
}
