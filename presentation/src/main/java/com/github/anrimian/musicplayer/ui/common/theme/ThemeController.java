package com.github.anrimian.musicplayer.ui.common.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class ThemeController {

    private static final String PREFERENCES_NAME = "theme_preferences";

    private static final String THEME_ID = "theme_id";

    private BehaviorSubject<AppTheme> themeSubject = BehaviorSubject.create();

    private final Context context;
    private final SharedPreferencesHelper preferences;

    public ThemeController(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void applyCurrentTheme(Activity activity) {
        AppTheme appTheme = getCurrentTheme();
        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);
    }

    public void applyCurrentSlidrTheme(Activity activity) {
        applyCurrentTheme(activity);
        activity.getTheme().applyStyle(R.style.SlidrActivityTheme, true);
    }

    public void setTheme(Activity activity, AppTheme appTheme) {
        if (appTheme == getCurrentTheme()) {
            return;
        }
        preferences.putInt(THEME_ID, appTheme.getId());

        activity.getTheme().applyStyle(appTheme.getThemeResId(), true);
        updateTaskManager(activity);

        activity.recreate();

        themeSubject.onNext(appTheme);
    }

    public AppTheme getCurrentTheme() {
        return AppTheme.getTheme(preferences.getInt(THEME_ID, 0));
    }

    @ColorInt
    public int getPrimaryThemeColor() {
        return ContextCompat.getColor(context, getCurrentTheme().getBackgroundColorId());
    }

    public Observable<AppTheme> getAppThemeObservable() {
        return withDefaultValue(themeSubject, this::getCurrentTheme);
    }

    private void updateTaskManager(Activity activity) {
        AndroidUtils.updateTaskManager(activity,
                R.string.app_name,
                activity.getApplicationInfo().icon,
                getColorFromAttr(activity, R.attr.colorPrimary));
    }
}
