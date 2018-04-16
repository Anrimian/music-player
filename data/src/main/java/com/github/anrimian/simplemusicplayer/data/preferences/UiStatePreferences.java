package com.github.anrimian.simplemusicplayer.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.simplemusicplayer.data.utils.preferences.SharedPreferencesHelper;

/**
 * Created on 16.04.2018.
 */
public class UiStatePreferences {

    public static final long NO_COMPOSITION = Long.MIN_VALUE;
    public static final int NO_POSITION = -1;

    private static final String PREFERENCES_NAME = "ui_preferences";

    private static final String TRACK_POSITION = "track_position";
    private static final String CURRENT_COMPOSITION_POSITION = "current_composition_position";
    private static final String CURRENT_COMPOSITION_ID = "current_composition_id";

    private final SharedPreferencesHelper preferences;

    public UiStatePreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void setTrackPosition(long position) {
        preferences.putLong(TRACK_POSITION, position);
    }

    public long getTrackPosition() {
        return preferences.getLong(TRACK_POSITION);
    }

    public void setCurrentCompositionId(long id) {
        preferences.putLong(CURRENT_COMPOSITION_ID, id);
    }

    public Long getCurrentCompositionId() {
        return preferences.getLong(CURRENT_COMPOSITION_ID, NO_COMPOSITION);
    }

    public void setCurrentCompositionPosition(int position) {
        preferences.putInt(CURRENT_COMPOSITION_POSITION, position);
    }

    public int getCurrentCompositionPosition() {
        return preferences.getInt(CURRENT_COMPOSITION_POSITION, NO_POSITION);
    }
}
