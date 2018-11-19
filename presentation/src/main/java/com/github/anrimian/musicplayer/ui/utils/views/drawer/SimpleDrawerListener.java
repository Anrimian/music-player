package com.github.anrimian.musicplayer.ui.utils.views.drawer;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;

public class SimpleDrawerListener implements DrawerLayout.DrawerListener {

    private Runnable onDrawerClosedListener;

    public SimpleDrawerListener(Runnable onDrawerClosedListener) {
        this.onDrawerClosedListener = onDrawerClosedListener;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        if (onDrawerClosedListener != null) {
            onDrawerClosedListener.run();
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
