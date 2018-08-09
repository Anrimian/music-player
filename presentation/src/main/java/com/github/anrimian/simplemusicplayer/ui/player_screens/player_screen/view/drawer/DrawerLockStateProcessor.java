package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.view.drawer;

import android.support.v4.widget.DrawerLayout;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;

public class DrawerLockStateProcessor {

    private final DrawerLayout drawer;

    private boolean openedBottomSheet = false;
    private boolean inRoot = false;

    public DrawerLockStateProcessor(DrawerLayout drawer) {
        this.drawer = drawer;
    }

    public void setBottomSheetOpen(boolean openedBottomSheet) {
        this.openedBottomSheet = openedBottomSheet;
        updateDrawerState();
    }

    public void setOnRootNavigationState(boolean inRoot) {
        this.inRoot = inRoot;
        updateDrawerState();
    }

    private void updateDrawerState() {
        boolean lock = openedBottomSheet || !inRoot;
        drawer.setDrawerLockMode(lock? LOCK_MODE_LOCKED_CLOSED: LOCK_MODE_UNLOCKED);
    }
}
