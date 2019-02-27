package com.github.anrimian.musicplayer.data.repositories.ui_state;

import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

/**
 * Created on 16.11.2017.
 */

public class UiStateRepositoryImpl implements UiStateRepository {

    private final UiStatePreferences uiStatePreferences;

    public UiStateRepositoryImpl(UiStatePreferences uiStatePreferences) {
        this.uiStatePreferences = uiStatePreferences;
    }

    @Override
    public void setTrackPosition(long position) {
        uiStatePreferences.setTrackPosition(position);
    }

    @Override
    public long getTrackPosition() {
        return uiStatePreferences.getTrackPosition();
    }

    @Override
    public void setPlayerPanelOpen(boolean open) {
        uiStatePreferences.setPlayerPanelOpen(open);
    }

    @Override
    public boolean isPlayerPanelOpen() {
        return uiStatePreferences.isPlayerPanelOpen();
    }

    @Override
    public void setSelectedDrawerScreen(int screenId) {
        uiStatePreferences.setSelectedDrawerScreen(screenId);
    }

    @Override
    public int getSelectedDrawerScreen() {
        return uiStatePreferences.getSelectedDrawerScreen();
    }

    @Override
    public void setSelectedLibraryScreen(int screenId) {
        uiStatePreferences.setSelectedLibraryScreen(screenId);
    }

    @Override
    public int getSelectedLibraryScreen() {
        return uiStatePreferences.getSelectedLibraryScreen();
    }

    @Override
    public void setSelectedFolderScreen(String path) {
        uiStatePreferences.setSelectedFolderScreen(path);
    }

    @Override
    public String getSelectedFolderScreen() {
        return uiStatePreferences.getSelectedFolderScreen();
    }

    @Override
    public void setSelectedPlayListScreenId(long playListId) {
        uiStatePreferences.setSelectedPlayListScreen(playListId);
    }

    @Override
    public long getSelectedPlayListScreenId() {
        return uiStatePreferences.getSelectedPlayListScreen();
    }
}
