package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;

import static androidx.core.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class LeftBottomShadowDelegate implements SlideDelegate {

    private final View leftShadow;
    private final View topLeftShadow;
    private final View bottomSheet;
    private final View bottomSheetCoordinator;

    public LeftBottomShadowDelegate(View leftShadow,
                                    View topLeftShadow,
                                    View bottomSheet,
                                    View bottomSheetCoordinator) {
        this.leftShadow = leftShadow;
        this.topLeftShadow = topLeftShadow;
        this.bottomSheet = bottomSheet;
        this.bottomSheetCoordinator = bottomSheetCoordinator;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(leftShadow)
                && isLaidOut(topLeftShadow)
                && isLaidOut(bottomSheet)
                && isLaidOut(bottomSheetCoordinator)) {
            moveView();
        } else {
            leftShadow.post(this::moveView);
        }
    }

    private void moveView() {
        float x = bottomSheetCoordinator.getX() - leftShadow.getWidth();
        leftShadow.setX(x);
        topLeftShadow.setX(x);

        leftShadow.setY(bottomSheet.getY());
        topLeftShadow.setY(bottomSheet.getY() - topLeftShadow.getHeight());
    }
}
