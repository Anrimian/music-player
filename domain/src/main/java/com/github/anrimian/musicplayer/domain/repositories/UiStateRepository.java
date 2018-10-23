package com.github.anrimian.musicplayer.domain.repositories;

/**
 * Created on 16.11.2017.
 */

public interface UiStateRepository {

    void setTrackPosition(long position);

    long getTrackPosition();

    void setPlayerPanelOpen(boolean open);

    boolean isPlayerPanelOpen();

    void setSelectedDrawerScreen(int screenId);

    int getSelectedDrawerScreen();

    void setSelectedLibraryScreen(int screenId);

    int getSelectedLibraryScreen();
}
