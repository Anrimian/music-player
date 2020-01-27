package com.github.anrimian.musicplayer.domain.repositories;

import io.reactivex.Observable;

/**
 * Created on 16.11.2017.
 */

public interface UiStateRepository {

    void setCurrentItemLastPosition(int position);

    int getCurrentItemLastPosition();

    void setTrackPosition(long position);

    long getTrackPosition();

    void setPlayerPanelOpen(boolean open);

    boolean isPlayerPanelOpen();

    void setSelectedDrawerScreen(int screenId);

    int getSelectedDrawerScreen();

    void setSelectedLibraryScreen(int screenId);

    int getSelectedLibraryScreen();

    void setSelectedFolderScreen(String path);

    String getSelectedFolderScreen();

    void setSelectedPlayListScreenId(long playListId);

    long getSelectedPlayListScreenId();

    void setCurrentQueueItemId(long id);

    Observable<Long> getCurrentItemIdObservable();

    long getCurrentQueueItemId();
}
