package com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import static android.support.v4.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class CollapseViewDelegate implements BottomSheetDelegate {

    private static final int UNDEFINED = -1;

    private int startHeight = UNDEFINED;
    private int startWidth = UNDEFINED;

    private View view;

    public CollapseViewDelegate(View view) {
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
        if (startHeight == UNDEFINED) {
            startHeight = view.getHeight();
            startWidth = view.getWidth();
        }
        int height = (int) (startHeight - (startHeight * slideOffset));
        int width = (int) (startWidth - (startWidth * slideOffset));
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        params.width = width;
        view.setLayoutParams(params);
        Log.d("ABDS", "width: " + width);
//        view.setAlpha(1 - slideOffset);
    }
}
