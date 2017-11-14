package com.github.anrimian.simplemusicplayer.data.repositories.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String PREFERENCES_NAME = "settings_preferences";

    private static final String RANDOM_PLAYING_ENABLED = "random_playing_enabled";
    private static final String INFINITE_PLAYING_ENABLED = "infinite_playing_enabled";

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    void setRandomPlayingEnabled(boolean enabled) {
        sharedPreferences.edit()
                .putBoolean(RANDOM_PLAYING_ENABLED, enabled)
                .apply();
    }

    boolean isRandomPlayingEnabled() {
        return sharedPreferences.getBoolean(RANDOM_PLAYING_ENABLED, false);
    }

    void setInfinitePlayingEnabled(boolean enabled) {
        sharedPreferences.edit()
                .putBoolean(INFINITE_PLAYING_ENABLED, enabled)
                .apply();
    }

    boolean isInfinitePlayingEnabled() {
        return sharedPreferences.getBoolean(INFINITE_PLAYING_ENABLED, false);
    }
}
