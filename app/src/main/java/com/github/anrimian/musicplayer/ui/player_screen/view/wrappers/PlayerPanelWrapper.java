package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers;

public interface PlayerPanelWrapper {

    boolean isBottomPanelExpanded();

    void collapseBottomPanel();

    void collapseBottomPanelSmoothly();

    void collapseBottomPanelSmoothly(Runnable doOnCollapse);

    void expandBottomPanel();

    void openPlayerPanel();
}
