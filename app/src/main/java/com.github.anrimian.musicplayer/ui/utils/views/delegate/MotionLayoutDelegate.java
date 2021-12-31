package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import androidx.constraintlayout.motion.widget.MotionLayout;

import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.isLaidOut;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

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
            if (motionLayout.getVisibility() != VISIBLE) {
                motionLayout.setVisibility(VISIBLE);
            }
            moveView(slideOffset);
        } else {
            motionLayout.post(() -> {
                animateVisibility(motionLayout, VISIBLE);
                moveView(slideOffset);
            });
        }
    }

    private void moveView(float slideOffset) {
        motionLayout.setProgress(slideOffset);
    }
}
