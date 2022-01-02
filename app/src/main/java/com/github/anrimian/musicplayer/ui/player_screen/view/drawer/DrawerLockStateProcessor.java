package com.github.anrimian.musicplayer.ui.player_screen.view.drawer;

import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentStackListener;

import androidx.drawerlayout.widget.DrawerLayout;

import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED;

public class DrawerLockStateProcessor {

    private final DrawerLayout drawer;

    private boolean openedBottomSheet = false;
    private boolean inRoot = true;
    private boolean isInSearchMode = false;
    private boolean isInSelectionMode = false;

    private final FragmentStackListener stackChangeListener = new StackChangeListenerImpl();

    private FragmentNavigation navigation;

    public DrawerLockStateProcessor(DrawerLayout drawer) {
        this.drawer = drawer;
    }

    public void setupWithNavigation(FragmentNavigation navigation) {
        this.navigation = navigation;
        onFragmentStackChanged(navigation.getScreensCount());
        navigation.addStackChangeListener(stackChangeListener);
    }

    public void release() {
        navigation.removeStackChangeListener(stackChangeListener);
    }

    public void onSearchModeChanged(boolean isInSearchMode) {
        this.isInSearchMode = isInSearchMode;
        updateDrawerLockState();
    }

    public void onBottomSheetOpened(boolean openedBottomSheet) {
        this.openedBottomSheet = openedBottomSheet;
        updateDrawerLockState();
    }

    public void onSelectionModeChanged(boolean isInSelection) {
        this.isInSelectionMode = isInSelection;
        updateDrawerLockState();
    }

    private void setOnRootNavigationState(boolean inRoot) {
        this.inRoot = inRoot;
        updateDrawerLockState();
    }

    private void onFragmentStackChanged(int stackSize) {
        setOnRootNavigationState(stackSize <= 1);
    }

    private void updateDrawerLockState() {
        boolean lock = openedBottomSheet || !inRoot || isInSearchMode || isInSelectionMode;
        drawer.setDrawerLockMode(lock? LOCK_MODE_LOCKED_CLOSED: LOCK_MODE_UNLOCKED);
    }

    private class StackChangeListenerImpl implements FragmentStackListener {

        @Override
        public void onStackChanged(int stackSize) {
            onFragmentStackChanged(stackSize);
        }
    }
}
