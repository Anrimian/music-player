package com.github.anrimian.musicplayer.ui.utils.views.delegate;

/**
 * Created on 21.01.2018.
 */

public class ReverseDelegate implements SlideDelegate {

    private final SlideDelegate delegate;

    public ReverseDelegate(SlideDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onSlide(float slideOffset) {
        delegate.onSlide(1 - slideOffset);
    }
}
