package com.github.anrimian.musicplayer.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.composition.Order;

import static com.github.anrimian.musicplayer.domain.models.composition.Order.ADD_TIME;
import static com.github.anrimian.musicplayer.domain.models.composition.Order.ADD_TIME_DESC;
import static com.github.anrimian.musicplayer.domain.models.composition.Order.valueOf;

/**
 * Created on 16.04.2018.
 */
public class SettingsPreferences {

    private static final String PREFERENCES_NAME = "settings_preferences";

    private static final String RANDOM_PLAYING_ENABLED = "random_playing_enabled";
    private static final String INFINITE_PLAYING_ENABLED = "infinite_playing_enabled";
    private static final String FOLDER_ORDER = "folder_order";
    private static final String COMPOSITIONS_ORDER = "compositions_order";

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

    public Order getFolderOrder() {
        return Order.fromId(preferences.getInt(FOLDER_ORDER, ADD_TIME_DESC.getId()));
    }

    public void setFolderOrder(Order order) {
        preferences.putInt(FOLDER_ORDER, order.getId());
    }

    public Order getCompositionsOrder() {
        return Order.fromId(preferences.getInt(COMPOSITIONS_ORDER, ADD_TIME_DESC.getId()));
    }

    public void setCompositionsOrder(Order order) {
        preferences.putInt(COMPOSITIONS_ORDER, order.getId());
    }
}
