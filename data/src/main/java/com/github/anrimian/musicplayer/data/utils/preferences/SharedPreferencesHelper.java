package com.github.anrimian.musicplayer.data.utils.preferences;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.collection.LruCache;

import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
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

    public ListPosition getListPosition(String key) {
        long positions = getLong(key);
        return new ListPosition((int) (positions >> 32), (int) positions);
    }

    public void putListPosition(String key, ListPosition listPosition) {
        long positions = (((long) listPosition.getPosition()) << 32) | (listPosition.getOffset() & 0xffffffffL);
        putLong(key, positions);
    }

    public <K, V> void putLruCache(String key, LruCache<K, V> cache) {
        Log.d("KEK", "putLruCache----------------");
        try {
            LinkedHashMap<K, V> map = (LinkedHashMap<K, V>) cache.snapshot();
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                JSONObject obj = new JSONObject();
                obj.put("key", entry.getKey());
                obj.put("value", entry.getValue());
                Log.d("KEK", "putLruCache: key: " + entry.getKey() + ", value: " + entry.getValue());
                jsonArray.put(obj);
            }
            putString(key, jsonArray.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> LruCache<K, V> getLruCache(String key, int maxCacheSize) {
        Log.d("KEK", "getLruCache----------------");
        try {
            LruCache<K, V> cache = new LruCache<>(maxCacheSize);

            String rawData = getString(key);
            if (rawData == null) {
                return cache;
            }
            JSONArray jsonArray = new JSONArray(rawData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                K cacheKey = (K) obj.get("key");
                V cacheValue = (V) obj.get("value");
                Log.d("KEK", "getLruCache: key: " + cacheKey + ", value: " + cacheValue);
                cache.put(cacheKey, cacheValue);
            }
            return cache;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
