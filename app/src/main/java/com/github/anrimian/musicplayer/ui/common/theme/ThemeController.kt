package com.github.anrimian.musicplayer.ui.common.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ThemeController(private val context: Context) {

    private val themeSubject = BehaviorSubject.create<AppTheme>()
    private val roundCoversSubject = BehaviorSubject.create<Boolean>()
    private val preferences = SharedPreferencesHelper(
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    )

    fun applyCurrentTheme(activity: Activity) {
        val appTheme = getCurrentUsedTheme()
        if (appTheme != themeSubject.value) {
            themeSubject.onNext(appTheme)
        }
        activity.theme.applyStyle(appTheme.themeResId, true)
        activity.theme.applyStyle(currentShapeStyle(), true)
        updateTaskManager(activity)
    }

    fun applyCurrentSlidrTheme(activity: Activity) {
        applyCurrentTheme(activity)
        activity.theme.applyStyle(R.style.SlidrActivityTheme, true)
    }

    fun setTheme(activity: Activity, appTheme: AppTheme) {
        val currentTheme = getCurrentTheme()
        if (appTheme == currentTheme) {
            return
        }
        if (currentTheme.isDark) {
            setLatestDarkTheme(currentTheme)
        }
        preferences.putInt(THEME_ID, appTheme.id)
        if (applyThemeRules(appTheme) != applyThemeRules(currentTheme)) {
            processThemeChange(activity, appTheme)
        }
    }

    fun setCircleShapeEnabled(activity: Activity, enabled: Boolean) {
        if (enabled == isCircleShapeEnabled()) {
            return
        }
        preferences.putBoolean(CIRCLE_SHAPE, enabled)
        roundCoversSubject.onNext(enabled)
        activity.recreate()
    }

    fun isCircleShapeEnabled() = preferences.getBoolean(CIRCLE_SHAPE, true)

    fun getCurrentTheme() = AppTheme.getTheme(preferences.getInt(THEME_ID, AppTheme.WHITE_PURPLE_TEAL.id))

    fun getPrimaryThemeColor() = ContextCompat.getColor(context, getCurrentUsedTheme().backgroundColorId)

    fun getAppThemeObservable(): Observable<AppTheme> = RxUtils.withDefaultValue(themeSubject) { getCurrentUsedTheme() }

    fun getRoundCoversObservable(): Observable<Boolean> = RxUtils.withDefaultValue(roundCoversSubject) { isCircleShapeEnabled() }

    fun setAutoDarkModeEnabled(activity: Activity, enabled: Boolean) {
        val currentTheme = getCurrentUsedTheme()
        preferences.putBoolean(AUTO_DARK_THEME, enabled)
        if (currentTheme != getCurrentUsedTheme()) {
            processThemeChange(activity, getCurrentTheme())
        }
    }

    fun isAutoDarkThemeEnabled() = preferences.getBoolean(AUTO_DARK_THEME, true)

    fun setFollowSystemThemeEnabled(activity: Activity, enabled: Boolean) {
        val currentTheme = getCurrentUsedTheme()
        preferences.putBoolean(FOLLOW_SYSTEM_THEME, enabled)
        if (currentTheme != getCurrentUsedTheme()) {
            processThemeChange(activity, getCurrentTheme())
        }
    }

    fun isFollowSystemThemeEnabled() = preferences.getBoolean(
        FOLLOW_SYSTEM_THEME,
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    )

    private fun currentShapeStyle() = if (isCircleShapeEnabled()) R.style.RoundedCorners else R.style.RoundedRectangleCorners

    private fun getLatestDarkTheme() = AppTheme.getTheme(preferences.getInt(LATEST_DARK_THEME_ID, AppTheme.DARK.id))

    private fun setLatestDarkTheme(appTheme: AppTheme) {
        preferences.putInt(LATEST_DARK_THEME_ID, appTheme.id)
    }

    private fun processThemeChange(activity: Activity, appTheme: AppTheme) {
        activity.theme.applyStyle(appTheme.themeResId, true)
        updateTaskManager(activity)
        activity.recreate()
        themeSubject.onNext(appTheme)
    }

    private fun getCurrentUsedTheme(): AppTheme {
        val theme = AppTheme.getTheme(preferences.getInt(THEME_ID, AppTheme.WHITE_PURPLE_TEAL.id))
        return applyThemeRules(theme)
    }

    private fun applyThemeRules(theme: AppTheme): AppTheme {
        var themeResult = theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isFollowSystemThemeEnabled()) {
            return if (isAutoDarkThemeEnabled() && isSystemNightModeEnabled()) {
                AppTheme.getSystemDarkTheme()
            } else AppTheme.getSystemWhiteTheme()
        }
        if (isAutoDarkThemeEnabled()
            && !themeResult.isDark
            && isSystemNightModeEnabled()
        ) {
            themeResult = getLatestDarkTheme()
        }
        return themeResult
    }

    private fun isSystemNightModeEnabled(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                == Configuration.UI_MODE_NIGHT_YES)
    }

    private fun updateTaskManager(activity: Activity) {
        AndroidUtils.updateTaskManager(
            activity,
            R.string.app_name,
            activity.applicationInfo.icon,
            AndroidUtils.getColorFromAttr(activity, R.attr.colorPrimary)
        )
    }

    private companion object {
        const val PREFERENCES_NAME = "theme_preferences"
        const val THEME_ID = "theme_id"
        const val LATEST_DARK_THEME_ID = "latest_dark_theme_id"
        const val AUTO_DARK_THEME = "auto_dark_theme"
        const val FOLLOW_SYSTEM_THEME = "follow_system_theme"
        const val CIRCLE_SHAPE = "circle_shape"
    }
}