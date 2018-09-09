package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.support.constraint.motion.MotionLayout;

import static android.support.v4.view.ViewCompat.isLaidOut;

/**
 * Created on 14.01.2018.
 */

public class MotionLayoutDelegate implements SlideDelegate {

    private final MotionLayout motionLayout;

    public MotionLayoutDelegate(MotionLayout motionLayout) {
        this.motionLayout = motionLayout;
    }


    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(motionLayout)) {
            moveView(slideOffset);
        } else {
            motionLayout.post(() -> moveView(slideOffset));
        }
    }

    private void moveView(float slideOffset) {
        motionLayout.setProgress(slideOffset);
    }
}
