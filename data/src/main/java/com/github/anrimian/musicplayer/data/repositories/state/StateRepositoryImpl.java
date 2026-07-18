package com.github.anrimian.musicplayer.data.repositories.state;

import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.IS_STORAGE_PLAYLISTS_IMPORTED;
import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.LAST_COMPLETE_SCAN_TIME;
import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.LAST_FILE_SCANNER_VERSION;
import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.PREFERENCES_NAME;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;

public class StateRepositoryImpl implements StateRepository {

    interface Constants {
        String PREFERENCES_NAME = "state_preferences";

        String LAST_FILE_SCANNER_VERSION = "last_file_scanner_version";
        String LAST_COMPLETE_SCAN_TIME = "last_complete_scan_time";
        String IS_STORAGE_PLAYLISTS_IMPORTED = "is_storage_playlists_imported";
    }

    private final SharedPreferencesHelper preferences;

    public StateRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public int getCurrentFileScannerVersion() {
        return 7;
    }

    @Override
    public void setLastFileScannerVersion(int version) {
        preferences.putInt(LAST_FILE_SCANNER_VERSION, version);
    }

    @Override
    public int getLastFileScannerVersion() {
        return preferences.getInt(LAST_FILE_SCANNER_VERSION);
    }

    @Override
    public long getLastCompleteScanTime() {
        return preferences.getLong(LAST_COMPLETE_SCAN_TIME);
    }

    @Override
    public void setLastCompleteScanTime(long scanTime) {
        preferences.putLong(LAST_COMPLETE_SCAN_TIME, scanTime);
    }

    @Override
    public boolean isStoragePlaylistsImported() {
        return preferences.getBoolean(Constants.IS_STORAGE_PLAYLISTS_IMPORTED);
    }

    @Override
    public void setStoragePlaylistsImported(boolean isImported) {
        preferences.putBoolean(IS_STORAGE_PLAYLISTS_IMPORTED, isImported);
    }

}
