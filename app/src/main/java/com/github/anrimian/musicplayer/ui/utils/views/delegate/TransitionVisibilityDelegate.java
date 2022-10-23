package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.isLaidOut;

import android.view.View;

/**
 * Created on 21.01.2018.
 */

public class TransitionVisibilityDelegate implements SlideDelegate {

    private final View view;
    private final float start;
    private final float endVisible;
    private final float startInvisible;
    private final float end;

    private final int invisibleState;

    public TransitionVisibilityDelegate(float start,
                                        float endVisible,
                                        float startInvisible,
                                        float end,
                                        View view) {
        this.start = start;
        this.end = end;
        this.endVisible = endVisible;
        this.startInvisible = startInvisible;
        this.view = view;
        invisibleState = INVISIBLE;
        if (start < 0.0f || end > 1.f || start > end) {
            throw new IllegalStateException("wrong values for start and end, start: " + start + ",  end: " + end);
        }
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
        float resultSlide = 1f;
        if (slideOffset <= start || slideOffset >= end) {
            resultSlide = 0f;
        } else if (slideOffset <= endVisible) {
            resultSlide = (slideOffset - start) / (endVisible - start);
        } else if (slideOffset >= startInvisible) {
            resultSlide = 1 - ((slideOffset - startInvisible) / (end - startInvisible));
        }

        view.setVisibility(resultSlide == 0 ? invisibleState : VISIBLE);
        view.setAlpha(resultSlide);
    }
}
