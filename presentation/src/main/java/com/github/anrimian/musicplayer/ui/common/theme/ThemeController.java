package com.github.anrimian.musicplayer.ui.common.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;

public class ThemeController {

    private static final String PREFERENCES_NAME = "theme_preferences";

    private static final String THEME_ID = "theme_id";

    private SharedPreferencesHelper preferences;

    public ThemeController(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void applyCurrentTheme(Activity activity) {
        AppTheme appTheme = AppTheme.getTheme(preferences.getInt(THEME_ID, 0));

        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
    }

    public void setTheme(Activity activity, AppTheme appTheme) {
        preferences.putInt(THEME_ID, appTheme.getId());

        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);

        //TODO animation
        activity.recreate();
    }
}
