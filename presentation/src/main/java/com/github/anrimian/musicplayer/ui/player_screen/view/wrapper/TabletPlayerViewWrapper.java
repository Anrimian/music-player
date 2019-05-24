package com.github.anrimian.musicplayer.ui.player_screen.view.wrapper;

import android.view.View;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import butterknife.ButterKnife;

public class TabletPlayerViewWrapper implements PlayerViewWrapper {

    public TabletPlayerViewWrapper(View view, Callback<Boolean> bottomSheetStateListener) {
        ButterKnife.bind(this, view);
        bottomSheetStateListener.call(false);
    }

    @Override
    public boolean isBottomPanelExpanded() {
        return false;
    }

    @Override
    public void collapseBottomPanel() {

    }

    @Override
    public void expandBottomPanel() {

    }

    @Override
    public void openPlayQueue() {

    }
}
