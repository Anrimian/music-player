package com.github.anrimian.simplemusicplayer.data.repositories.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.simplemusicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

/**
 * Created on 14.11.2017.
 */

public class SettingsRepositoryImpl implements SettingsRepository {

    private static final String PREFERENCES_NAME = "settings_preferences";

    private static final String RANDOM_PLAYING_ENABLED = "random_playing_enabled";
    private static final String INFINITE_PLAYING_ENABLED = "infinite_playing_enabled";

    private SharedPreferencesHelper preferences;

    public SettingsRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        preferences.putBoolean(RANDOM_PLAYING_ENABLED, enabled);
    }

    @Override
    public boolean isRandomPlayingEnabled() {
        return preferences.getBoolean(RANDOM_PLAYING_ENABLED);
    }

    @Override
    public void setInfinitePlayingEnabled(boolean enabled) {
        preferences.putBoolean(INFINITE_PLAYING_ENABLED, enabled);
    }

    @Override
    public boolean isInfinitePlayingEnabled() {
        return preferences.getBoolean(INFINITE_PLAYING_ENABLED);
    }
}
