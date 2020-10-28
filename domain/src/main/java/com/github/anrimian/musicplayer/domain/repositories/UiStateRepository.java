package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;

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

    void setSelectedFolderScreen(@Nullable Long folderId);

    @Nullable
    Long getSelectedFolderScreen();

    void setSelectedPlayListScreenId(long playListId);

    long getSelectedPlayListScreenId();

    void setCurrentQueueItemId(long id);

    Observable<Long> getCurrentItemIdObservable();

    long getCurrentQueueItemId();

    ListPosition getSavedCompositionsListPosition();

    void saveCompositionsListPosition(ListPosition listPosition);

    ListPosition getSavedArtistsListPosition();

    void saveArtistsListPosition(ListPosition listPosition);

    ListPosition getSavedAlbumsListPosition();

    void saveAlbumsListPosition(ListPosition listPosition);

    ListPosition getSavedPlaylistsPosition();

    void savePlaylistsPosition(ListPosition listPosition);

    void saveFolderListPosition(@Nullable Long folderId, ListPosition listPosition);

    ListPosition getSavedFolderListPosition(@Nullable Long folderId);

    void saveAlbumListPosition(@Nullable Long id, ListPosition listPosition);

    ListPosition getSavedAlbumListPosition(@Nullable Long id);

    void saveArtistListPosition(@Nullable Long id, ListPosition listPosition);

    ListPosition getSavedArtistListPosition(@Nullable Long id);

    void savePlaylistsListPosition(@Nullable Long id, ListPosition listPosition);

    ListPosition getSavedPlaylistListPosition(@Nullable Long id);
}
