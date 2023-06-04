package com.github.anrimian.musicplayer.ui.common.theme;

import static com.github.anrimian.musicplayer.domain.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ThemeController {

    private static final String PREFERENCES_NAME = "theme_preferences";

    private static final String THEME_ID = "theme_id";
    private static final String LATEST_DARK_THEME_ID = "latest_dark_theme_id";
    private static final String AUTO_DARK_THEME = "auto_dark_theme";
    private static final String FOLLOW_SYSTEM_THEME = "follow_system_theme";

    private final BehaviorSubject<AppTheme> themeSubject = BehaviorSubject.create();

    private final Context context;
    private final SharedPreferencesHelper preferences;

    public ThemeController(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void applyCurrentTheme(Activity activity) {
        AppTheme appTheme = getCurrentUsedTheme();
        if (!appTheme.equals(themeSubject.getValue())) {
            themeSubject.onNext(appTheme);
        }
        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);
    }

    public void applyCurrentSlidrTheme(Activity activity) {
        applyCurrentTheme(activity);
        activity.getTheme().applyStyle(R.style.SlidrActivityTheme, true);
    }

    public void setTheme(Activity activity, AppTheme appTheme) {
        AppTheme currentTheme = getCurrentTheme();
        if (appTheme.equals(currentTheme)) {
            return;
        }
        if (currentTheme.isDark()) {
            setLatestDarkTheme(currentTheme);
        }
        preferences.putInt(THEME_ID, appTheme.getId());

        if (!applyThemeRules(appTheme).equals(applyThemeRules(currentTheme))) {
            processThemeChange(activity, appTheme);
        }
    }

    public AppTheme getCurrentTheme() {
        return AppTheme.getTheme(preferences.getInt(THEME_ID, AppTheme.WHITE_PURPLE_TEAL.getId()));
    }

    @ColorInt
    public int getPrimaryThemeColor() {
        return ContextCompat.getColor(context, getCurrentUsedTheme().getBackgroundColorId());
    }

    public Observable<AppTheme> getAppThemeObservable() {
        return withDefaultValue(themeSubject, this::getCurrentUsedTheme);
    }

    public void setAutoDarkModeEnabled(Activity activity, boolean enabled) {
        AppTheme currentTheme = getCurrentUsedTheme();
        preferences.putBoolean(AUTO_DARK_THEME, enabled);
        if (!currentTheme.equals(getCurrentUsedTheme())) {
            processThemeChange(activity, getCurrentTheme());
        }
    }

    public boolean isAutoDarkThemeEnabled() {
        return preferences.getBoolean(AUTO_DARK_THEME, true);
    }

    public void setFollowSystemThemeEnabled(Activity activity, boolean enabled) {
        AppTheme currentTheme = getCurrentUsedTheme();
        preferences.putBoolean(FOLLOW_SYSTEM_THEME, enabled);
        if (!currentTheme.equals(getCurrentUsedTheme())) {
            processThemeChange(activity, getCurrentTheme());
        }
    }

    public boolean isFollowSystemThemeEnabled() {
        return preferences.getBoolean(FOLLOW_SYSTEM_THEME, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);
    }

    private AppTheme getLatestDarkTheme() {
        return AppTheme.getTheme(preferences.getInt(LATEST_DARK_THEME_ID, AppTheme.DARK.getId()));
    }

    private void setLatestDarkTheme(AppTheme appTheme) {
        preferences.putInt(LATEST_DARK_THEME_ID, appTheme.getId());
    }

    private void processThemeChange(Activity activity, AppTheme appTheme) {
        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);

        activity.recreate();

        themeSubject.onNext(appTheme);
    }

    private AppTheme getCurrentUsedTheme() {
        AppTheme theme = AppTheme.getTheme(preferences.getInt(THEME_ID, AppTheme.WHITE_PURPLE_TEAL.getId()));
        return applyThemeRules(theme);
    }

    private AppTheme applyThemeRules(AppTheme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isFollowSystemThemeEnabled()) {
            if (isAutoDarkThemeEnabled() && isSystemNightModeEnabled()) {
                return AppTheme.getSystemDarkTheme();
            }
            return AppTheme.getSystemWhiteTheme();
        }
        if (isAutoDarkThemeEnabled()
                && !theme.isDark()
                && isSystemNightModeEnabled()) {
            theme = getLatestDarkTheme();
        }
        return theme;
    }

    private boolean isSystemNightModeEnabled() {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void updateTaskManager(Activity activity) {
        AndroidUtils.updateTaskManager(activity,
                R.string.app_name,
                activity.getApplicationInfo().icon,
                getColorFromAttr(activity, R.attr.colorPrimary));
    }
}
