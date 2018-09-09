package com.github.anrimian.musicplayer.data.utils.preferences;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Map;
import java.util.Set;

/**
 * Created on 16.11.2017.
 */

@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
public class SharedPreferencesHelper {

    private SharedPreferences preferences;

    public SharedPreferencesHelper(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void putInt(String key, int val) {
        preferences.edit().putInt(key, val).apply();
    }

    public void putString(String key, String val) {
        preferences.edit().putString(key, val).apply();
    }

    public void putBoolean(String key, boolean val) {
        preferences.edit().putBoolean(key, val).apply();
    }

    public void putFloat(String key, float val) {
        preferences.edit().putFloat(key, val).apply();
    }

    public void putLong(String key, long val) {
        preferences.edit().putLong(key, val).apply();
    }

    public void putStringSet(String key, Set<String> val) {
        preferences.edit().putStringSet(key, val).apply();
    }

    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int intDefaultVal) {
        return preferences.getInt(key, intDefaultVal);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String stringDefaultVal) {
        return preferences.getString(key, stringDefaultVal);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    public float getFloat(String key) {
        return preferences.getFloat(key, 0F);
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public long getLong(String key) {
        return preferences.getLong(key, 0L);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return preferences.getStringSet(key, defaultValue);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, null);
    }
}
