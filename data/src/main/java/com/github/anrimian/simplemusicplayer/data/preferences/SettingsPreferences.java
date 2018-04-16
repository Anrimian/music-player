package com.github.anrimian.simplemusicplayer.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.simplemusicplayer.data.utils.preferences.SharedPreferencesHelper;

/**
 * Created on 16.04.2018.
 */
public class SettingsPreferences {

    private static final String PREFERENCES_NAME = "settings_preferences";

    private static final String RANDOM_PLAYING_ENABLED = "random_playing_enabled";
    private static final String INFINITE_PLAYING_ENABLED = "infinite_playing_enabled";

    private SharedPreferencesHelper preferences;

    public SettingsPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void setRandomPlayingEnabled(boolean enabled) {
        preferences.putBoolean(RANDOM_PLAYING_ENABLED, enabled);
    }

    public boolean isRandomPlayingEnabled() {
        return preferences.getBoolean(RANDOM_PLAYING_ENABLED);
    }

    public void setInfinitePlayingEnabled(boolean enabled) {
        preferences.putBoolean(INFINITE_PLAYING_ENABLED, enabled);
    }

    public boolean isInfinitePlayingEnabled() {
        return preferences.getBoolean(INFINITE_PLAYING_ENABLED);
    }
}
