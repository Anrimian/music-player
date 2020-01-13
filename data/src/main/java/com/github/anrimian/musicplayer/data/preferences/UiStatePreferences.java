package com.github.anrimian.musicplayer.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.Screens;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.CURRENT_QUEUE_ITEM_ID;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.CURRENT_QUEUE_ITEM_LAST_POSITION;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.IS_PLAYER_PANEL_OPEN;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.PREFERENCES_NAME;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.SELECTED_DRAWER_SCREEN;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.SELECTED_FOLDER_SCREEN;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.SELECTED_LIBRARY_SCREEN;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.SELECTED_PLAYLIST_SCREEN;
import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.Constants.TRACK_POSITION;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;

/**
 * Created on 16.04.2018.
 */
public class UiStatePreferences {

    public static final long NO_ITEM = Long.MIN_VALUE;

    interface Constants {
        String PREFERENCES_NAME = "ui_preferences";

        String TRACK_POSITION = "track_position";
        String CURRENT_QUEUE_ITEM_ID = "current_play_queue_id";
        String CURRENT_QUEUE_ITEM_LAST_POSITION = "current_queue_item_last_position";
        String SELECTED_DRAWER_SCREEN = "selected_drawer_screen";
        String SELECTED_LIBRARY_SCREEN = "selected_library_screen";
        String IS_PLAYER_PANEL_OPEN = "is_player_panel_open";
        String SELECTED_FOLDER_SCREEN = "selected_folder_screen";
        String SELECTED_PLAYLIST_SCREEN = "selected_playlist_screen";
    }

    private final BehaviorSubject<Long> currentItemSubject = BehaviorSubject.create();

    private final SharedPreferencesHelper preferences;

    public UiStatePreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void setCurrentItemLastPosition(int position) {
        preferences.putInt(CURRENT_QUEUE_ITEM_LAST_POSITION, position);
    }

    public int getCurrentItemLastPosition() {
        return preferences.getInt(CURRENT_QUEUE_ITEM_LAST_POSITION);
    }

    public void setPlayerPanelOpen(boolean open) {
        preferences.putBoolean(IS_PLAYER_PANEL_OPEN, open);
    }

    public boolean isPlayerPanelOpen() {
        return preferences.getBoolean(IS_PLAYER_PANEL_OPEN, false);
    }

    public void setSelectedDrawerScreen(int screenId) {
        preferences.putInt(SELECTED_DRAWER_SCREEN, screenId);
    }

    public int getSelectedDrawerScreen() {
        return preferences.getInt(SELECTED_DRAWER_SCREEN, Screens.LIBRARY);
    }

    public void setSelectedLibraryScreen(int screenId) {
        preferences.putInt(SELECTED_LIBRARY_SCREEN, screenId);
    }

    public int getSelectedLibraryScreen() {
        return preferences.getInt(SELECTED_LIBRARY_SCREEN, Screens.LIBRARY_COMPOSITIONS);
    }

    public void setTrackPosition(long position) {
        preferences.putLong(TRACK_POSITION, position);
    }

    public long getTrackPosition() {
        return preferences.getLong(TRACK_POSITION);
    }

    public void setCurrentQueueItemId(long id) {
        preferences.putLong(CURRENT_QUEUE_ITEM_ID, id);
        currentItemSubject.onNext(id);
    }

    public Observable<Long> getCurrentItemIdObservable() {
        return withDefaultValue(currentItemSubject, this::getCurrentQueueItemId)
                .distinctUntilChanged();
    }

    public long getCurrentQueueItemId() {
        Long cachedValue = currentItemSubject.getValue();
        if (cachedValue != null) {
            return cachedValue;
        }
        return preferences.getLong(CURRENT_QUEUE_ITEM_ID, NO_ITEM);
    }

    public void setSelectedFolderScreen(String path) {
        preferences.putString(SELECTED_FOLDER_SCREEN, path);
    }

    public String getSelectedFolderScreen() {
        return preferences.getString(SELECTED_FOLDER_SCREEN);
    }

    public void setSelectedPlayListScreen(long playListId) {
        preferences.putLong(SELECTED_PLAYLIST_SCREEN, playListId);
    }

    public long getSelectedPlayListScreen() {
        return preferences.getLong(SELECTED_PLAYLIST_SCREEN);
    }
}
