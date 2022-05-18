package com.github.anrimian.musicplayer.ui.common.locale

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper
import java.util.*


private const val PREFERENCES_NAME = "locale_preferences"

private const val CURRENT_LANGUAGE_CODE = "current_language_code"
private const val SYSTEM_LANGUAGE = "system_language"

//update when android 13 will be released
//https://developer.android.com/about/versions/13/features/app-languages
class LocaleController(private val context: Context) {

    private val preferences = SharedPreferencesHelper(
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    )

    fun dispatchAttachBaseContext(baseContext: Context): Context {
        val languageCode = getCurrentLocaleCode()
        if (languageCode == SYSTEM_LANGUAGE) {
            return baseContext
        }

        val locale = Locale(getCurrentLocaleCode())
        Locale.setDefault(locale)

        val resources = baseContext.resources
        val config = Configuration(resources.configuration)

        config.setLocale(locale)
        return baseContext.createConfigurationContext(config)
    }

    fun setCurrentLocale(locale: Locale?, activity: Activity) {
        val localeCode = locale?.toLanguageTag() ?: SYSTEM_LANGUAGE
        if (localeCode == getCurrentLocaleCode()) {
            return
        }
        preferences.putString(CURRENT_LANGUAGE_CODE, localeCode)
        activity.recreate()
    }

    fun getCurrentLocaleName(): String {
        val code = getCurrentLocaleCode()
        return if (code == SYSTEM_LANGUAGE) {
            context.getString(R.string.follow_system_language)
        } else {
            Locale.forLanguageTag(code).displayName
        }
    }

    private fun getCurrentLocaleCode(): String {
        return preferences.getString(CURRENT_LANGUAGE_CODE, SYSTEM_LANGUAGE)
    }

}