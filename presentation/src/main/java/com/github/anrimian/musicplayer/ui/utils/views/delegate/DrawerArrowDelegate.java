package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.support.v7.graphics.drawable.DrawerArrowDrawable;

public class DrawerArrowDelegate implements SlideDelegate {

    private final DrawerArrowDrawable drawerArrowDrawable;
    private LockCallback lockCallback;

    public DrawerArrowDelegate(DrawerArrowDrawable drawerArrowDrawable,
                               LockCallback lockCallback) {
        this.drawerArrowDrawable = drawerArrowDrawable;
        this.lockCallback = lockCallback;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (lockCallback == null || !lockCallback.isLocked()) {
            drawerArrowDrawable.setProgress(slideOffset);
        }
    }

    public interface LockCallback {
        boolean isLocked();
    }

}
