package com.github.anrimian.musicplayer.ui.utils.views.delegate;

public class LockDelegate implements SlideDelegate {

    private final SlideDelegate slideDelegate;
    private final LockCallback lockCallback;

    public LockDelegate(SlideDelegate slideDelegate,
                        LockCallback lockCallback) {
        this.slideDelegate = slideDelegate;
        this.lockCallback = lockCallback;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (!lockCallback.isLocked()) {
            slideDelegate.onSlide(slideOffset);
        }
    }

    public interface LockCallback {
        boolean isLocked();
    }

}
