package com.github.anrimian.simplemusicplayer.ui.utils.views.delegate;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.view.View;

import java.util.concurrent.locks.Lock;

import static android.support.v4.view.ViewCompat.isLaidOut;

public class DrawerArrowBottomSheetDelegate implements BottomSheetDelegate {

    private final DrawerArrowDrawable drawerArrowDrawable;
    private LockCallback lockCallback;

    public DrawerArrowBottomSheetDelegate(DrawerArrowDrawable drawerArrowDrawable,
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
