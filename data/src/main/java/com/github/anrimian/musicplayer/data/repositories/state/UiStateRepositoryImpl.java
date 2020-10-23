package com.github.anrimian.musicplayer.data.repositories.state;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.Screens;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.CURRENT_QUEUE_ITEM_ID;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.CURRENT_QUEUE_ITEM_LAST_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.IS_PLAYER_PANEL_OPEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ALBUMS_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ARTISTS_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_COMPOSITIONS_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.PREFERENCES_NAME;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_DRAWER_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_FOLDER_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_LIBRARY_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_PLAYLIST_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.TRACK_POSITION;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;

/**
 * Created on 16.11.2017.
 */

public class UiStateRepositoryImpl implements UiStateRepository {

    public static final long NO_ITEM = Long.MIN_VALUE;

    interface Constants {
        String PREFERENCES_NAME = "ui_preferences";

        String TRACK_POSITION = "track_position";
        String CURRENT_QUEUE_ITEM_ID = "current_play_queue_id";
        String CURRENT_QUEUE_ITEM_LAST_POSITION = "current_queue_item_last_position";
        String SELECTED_DRAWER_SCREEN = "selected_drawer_screen";
        String SELECTED_LIBRARY_SCREEN = "selected_library_screen";
        String IS_PLAYER_PANEL_OPEN = "is_player_panel_open";
        String SELECTED_FOLDER_SCREEN = "selected_folder_screen_id";
        String SELECTED_PLAYLIST_SCREEN = "selected_playlist_screen";
        String LIBRARY_COMPOSITIONS_POSITION = "library_compositions_position";
        String LIBRARY_ARTISTS_POSITION = "library_artists_position";
        String LIBRARY_ALBUMS_POSITION = "library_albums_position";
    }

    private final BehaviorSubject<Long> currentItemSubject = BehaviorSubject.create();

    private final SharedPreferencesHelper preferences;

    public UiStateRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public void setCurrentItemLastPosition(int position) {
        preferences.putInt(CURRENT_QUEUE_ITEM_LAST_POSITION, position);
    }

    @Override
    public int getCurrentItemLastPosition() {
        return preferences.getInt(CURRENT_QUEUE_ITEM_LAST_POSITION);
    }

    @Override
    public void setTrackPosition(long position) {
        preferences.putLong(TRACK_POSITION, position);
    }

    @Override
    public long getTrackPosition() {
        return preferences.getLong(TRACK_POSITION);
    }

    @Override
    public void setPlayerPanelOpen(boolean open) {
        preferences.putBoolean(IS_PLAYER_PANEL_OPEN, open);
    }

    @Override
    public boolean isPlayerPanelOpen() {
        return preferences.getBoolean(IS_PLAYER_PANEL_OPEN, false);
    }

    @Override
    public void setSelectedDrawerScreen(int screenId) {
        preferences.putInt(SELECTED_DRAWER_SCREEN, screenId);
    }

    @Override
    public int getSelectedDrawerScreen() {
        return preferences.getInt(SELECTED_DRAWER_SCREEN, Screens.LIBRARY);
    }

    @Override
    public void setSelectedLibraryScreen(int screenId) {
        preferences.putInt(SELECTED_LIBRARY_SCREEN, screenId);
    }

    @Override
    public int getSelectedLibraryScreen() {
        return preferences.getInt(SELECTED_LIBRARY_SCREEN, Screens.LIBRARY_COMPOSITIONS);
    }

    @Override
    public void setSelectedFolderScreen(@Nullable Long folderId) {
        preferences.putLong(SELECTED_FOLDER_SCREEN, folderId == null? 0: folderId);
    }

    @Override
    public Long getSelectedFolderScreen() {
        long id = preferences.getLong(SELECTED_FOLDER_SCREEN);
        return id == 0? null: id;
    }

    @Override
    public void setSelectedPlayListScreenId(long playListId) {
        preferences.putLong(SELECTED_PLAYLIST_SCREEN, playListId);
    }

    @Override
    public long getSelectedPlayListScreenId() {
        return preferences.getLong(SELECTED_PLAYLIST_SCREEN);
    }

    @Override
    public void setCurrentQueueItemId(long id) {
        preferences.putLong(CURRENT_QUEUE_ITEM_ID, id);
        currentItemSubject.onNext(id);
    }

    @Override
    public Observable<Long> getCurrentItemIdObservable() {
        return withDefaultValue(currentItemSubject, this::getCurrentQueueItemId)
                .distinctUntilChanged();
    }

    @Override
    public long getCurrentQueueItemId() {
        Long cachedValue = currentItemSubject.getValue();
        if (cachedValue != null) {
            return cachedValue;
        }
        return preferences.getLong(CURRENT_QUEUE_ITEM_ID, NO_ITEM);
    }

    @Override
    public ListPosition getSavedCompositionsListPosition() {
        return preferences.getListPosition(LIBRARY_COMPOSITIONS_POSITION);
    }

    @Override
    public void saveCompositionsListPosition(ListPosition listPosition) {
        preferences.putListPosition(LIBRARY_COMPOSITIONS_POSITION, listPosition);
    }

    @Override
    public ListPosition getSavedArtistsListPosition() {
        return preferences.getListPosition(LIBRARY_ARTISTS_POSITION);
    }

    @Override
    public void saveArtistsListPosition(ListPosition listPosition) {
        preferences.putListPosition(LIBRARY_ARTISTS_POSITION, listPosition);
    }

    @Override
    public ListPosition getSavedAlbumsListPosition() {
        return preferences.getListPosition(LIBRARY_ALBUMS_POSITION);
    }

    @Override
    public void saveAlbumsListPosition(ListPosition listPosition) {
        preferences.putListPosition(LIBRARY_ALBUMS_POSITION, listPosition);
    }
}
