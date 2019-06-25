package com.github.anrimian.musicplayer.ui.common.theme;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;

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
        AppTheme appTheme = AppTheme.getTheme(preferences.getInt(THEME_ID, 0));

        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);
    }

    public void setTheme(Activity activity, AppTheme appTheme) {
        preferences.putInt(THEME_ID, appTheme.getId());

        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);

        activity.recreate();
    }

    private void updateTaskManager(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription taskDescription;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                taskDescription = new ActivityManager.TaskDescription(
                        activity.getString(R.string.app_name),
                        R.mipmap.ic_launcher,
                        getColorFromAttr(activity, R.attr.colorPrimary));
            } else {
                taskDescription = new ActivityManager.TaskDescription(
                        activity.getString(R.string.app_name),
                        BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher),
                        getColorFromAttr(activity, R.attr.colorPrimary));
            }
            activity.setTaskDescription(taskDescription);
        }
    }
}
