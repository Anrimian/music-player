package com.github.anrimian.musicplayer.ui.player_screen.view.drawer;

import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;

public class DrawerLockStateProcessor {

    private final DrawerLayout drawer;

    private boolean openedBottomSheet = false;
    private boolean inRoot = true;

    private FragmentManager fragmentManager;

    public DrawerLockStateProcessor(DrawerLayout drawer) {
        this.drawer = drawer;
    }

    public void setupWithFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        onFragmentStackChanged();
        fragmentManager.addOnBackStackChangedListener(this::onFragmentStackChanged);
    }

    public void onBottomSheetOpened(boolean openedBottomSheet) {
        this.openedBottomSheet = openedBottomSheet;
        updateDrawerState();
    }

    private void setOnRootNavigationState(boolean inRoot) {
        this.inRoot = inRoot;
        updateDrawerState();
    }

    private void onFragmentStackChanged() {
        setOnRootNavigationState(fragmentManager.getBackStackEntryCount() == 0);
    }

    private void updateDrawerState() {
        boolean lock = openedBottomSheet || !inRoot;
        drawer.setDrawerLockMode(lock? LOCK_MODE_LOCKED_CLOSED: LOCK_MODE_UNLOCKED);
    }
}
