package com.github.anrimian.simplemusicplayer.ui.utils.views.delegate;

import android.support.v7.app.ActionBarDrawerToggle;

public class DrawerToggleBottomSheetDelegate implements BottomSheetDelegate {

    private final ActionBarDrawerToggle drawerToggle;

    public DrawerToggleBottomSheetDelegate(ActionBarDrawerToggle drawerToggle) {
        this.drawerToggle = drawerToggle;
    }

    @Override
    public void onSlide(float slideOffset) {
        drawerToggle.getDrawerArrowDrawable().setProgress(slideOffset);
    }

}
