package com.github.anrimian.simplemusicplayer.data.repositories.ui_state;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.simplemusicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

/**
 * Created on 16.11.2017.
 */

public class UiStateRepositoryImpl implements UiStateRepository {

    private static final String PREFERENCES_NAME = "ui_preferences";

    private static final String TRACK_POSITION = "track_position";
    private static final String PLAY_LIST_POSITION = "play_list_position";

    private SharedPreferencesHelper preferences;

    public UiStateRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
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
    public void setPlayListPosition(int position) {
        preferences.putInt(PLAY_LIST_POSITION, position);
    }

    @Override
    public int getPlayListPosition() {
        return preferences.getInt(PLAY_LIST_POSITION);
    }
}
