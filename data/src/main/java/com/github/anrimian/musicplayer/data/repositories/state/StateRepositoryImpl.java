package com.github.anrimian.musicplayer.data.repositories.state;

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
        return 0;
    }

    @Override
    public void setLastFileScannerVersion() {

    }

    @Override
    public int getLastFileScannerVersion() {
        return 0;
    }

    @Override
    public long getLastFullScanTime() {
        return 0;
    }

    @Override
    public void setLastFullScanTime() {

    }
}
