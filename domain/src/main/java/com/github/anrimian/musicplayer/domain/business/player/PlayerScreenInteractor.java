package com.github.anrimian.musicplayer.domain.business.player;

import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

public class PlayerScreenInteractor {

    private final UiStateRepository uiStateRepository;

    public PlayerScreenInteractor(UiStateRepository uiStateRepository) {
        this.uiStateRepository = uiStateRepository;
    }

    public void setPlayerPanelOpen(boolean open) {
        uiStateRepository.setPlayerPanelOpen(open);
    }

    public boolean isPlayerPanelOpen() {
        return uiStateRepository.isPlayerPanelOpen();
    }

    public void setSelectedDrawerScreen(int screenId) {
        uiStateRepository.setSelectedDrawerScreen(screenId);
    }

    public int getSelectedDrawerScreen() {
        return uiStateRepository.getSelectedDrawerScreen();
    }

    public long getSelectedPlayListScreenId() {
        return uiStateRepository.getSelectedPlayListScreenId();
    }

    public int getSelectedLibraryScreen() {
        return uiStateRepository.getSelectedLibraryScreen();
    }
}
