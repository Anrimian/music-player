package com.github.anrimian.musicplayer.ui.common.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class ThemeController {

    private static final String PREFERENCES_NAME = "theme_preferences";

    private static final String THEME_ID = "theme_id";

    private SharedPreferencesHelper preferences;

    public ThemeController(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void applyCurrentTheme(Activity activity) {
        AppTheme appTheme = getCurrentTheme();
        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);
    }

    public void setTheme(Activity activity, AppTheme appTheme) {
        if (appTheme == getCurrentTheme()) {
            return;
        }
        preferences.putInt(THEME_ID, appTheme.getId());

        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);

        activity.recreate();
    }

    public AppTheme getCurrentTheme() {
        return AppTheme.getTheme(preferences.getInt(THEME_ID, 0));
    }

    private void updateTaskManager(Activity activity) {
        AndroidUtils.updateTaskManager(activity,
                R.string.app_name,
                R.mipmap.ic_launcher,
                getColorFromAttr(activity, R.attr.colorPrimary));
    }
}
