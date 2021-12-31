package com.github.anrimian.musicplayer.ui.utils.views.delegate;

/**
 * Created on 21.01.2018.
 */

public class BoundValuesDelegate implements SlideDelegate {

    private final float start;
    private final float end;

    private final SlideDelegate delegate;

    public BoundValuesDelegate(float start, float end, SlideDelegate delegate) {
        this.start = start;
        this.end = end;
        this.delegate = delegate;
        if (start < 0.0f || end > 1.f || start > end) {
            throw new IllegalStateException("wrong values for start and end, start: " + start + ",  end: " + end);
        }
    }

    @Override
    public void onSlide(float slideOffset) {
        float resultSlide;
        if (slideOffset <= start) {
            resultSlide = 0.0f;
        } else if (slideOffset >= end) {
            resultSlide = 1.0f;
        } else {
            resultSlide = (slideOffset - start) / (end - start);
        }
        delegate.onSlide(resultSlide);
    }
}
