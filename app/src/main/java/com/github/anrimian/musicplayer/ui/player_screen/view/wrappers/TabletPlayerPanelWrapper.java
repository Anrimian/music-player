package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers;

import android.view.View;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;


public class TabletPlayerPanelWrapper implements PlayerPanelWrapper {

    public TabletPlayerPanelWrapper(View view,
                                    AdvancedToolbar toolbar,
                                    Callback<Boolean> bottomSheetStateListener) {
        bottomSheetStateListener.call(false);
        toolbar.setContentVisible(true);
    }

    @Override
    public boolean isBottomPanelExpanded() {
        return false;
    }

    @Override
    public void collapseBottomPanelSmoothly() {

    }

    @Override
    public void collapseBottomPanelSmoothly(Runnable doOnCollapse) {
        doOnCollapse.run();
    }

    @Override
    public void collapseBottomPanel() {

    }

    @Override
    public void expandBottomPanel() {

    }

    @Override
    public void openPlayerPanel() {

    }
}
