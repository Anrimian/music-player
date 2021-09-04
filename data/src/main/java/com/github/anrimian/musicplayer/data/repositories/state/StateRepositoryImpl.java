package com.github.anrimian.musicplayer.data.repositories.state;

import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.LAST_COMPLETE_SCAN_TIME;
import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.LAST_FILE_SCANNER_VERSION;
import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.PREFERENCES_NAME;
import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.ROOT_FOLDER_PATH;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;

public class StateRepositoryImpl implements StateRepository {

    interface Constants {
        String PREFERENCES_NAME = "state_preferences";

        String ROOT_FOLDER_PATH = "root_folder_path";
        String LAST_FILE_SCANNER_VERSION = "last_file_scanner_version";
        String LAST_COMPLETE_SCAN_TIME = "last_complete_scan_time";
    }

    private final SharedPreferencesHelper preferences;

    public StateRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public String getRootFolderPath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return null;
        }
        return preferences.getString(ROOT_FOLDER_PATH);
    }

    @Override
    public void setRootFolderPath(String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return;
        }
        preferences.putString(ROOT_FOLDER_PATH, path);
    }

    @Override
    public int getCurrentFileScannerVersion() {
        return 1;
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
}
