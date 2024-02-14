package com.github.anrimian.musicplayer.data.repositories.logger

import android.content.Context
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository

class LoggerRepositoryImpl(context: Context) : LoggerRepository {

    private val preferences: SharedPreferencesHelper

    init {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        preferences = SharedPreferencesHelper(sharedPreferences)
    }

    override fun wasFatalError(): Boolean {
        return preferences.getBoolean(WAS_FATAL_ERROR, false)
    }

    override fun setWasFatalError(wasFatalError: Boolean) {
        preferences.putBoolean(WAS_FATAL_ERROR, wasFatalError)
    }

    override fun wasCriticalFatalError(): Boolean {
        return preferences.getBoolean(WAS_CRITICAL_FATAL_ERROR, false)
    }

    override fun setWasCriticalFatalError(wasCriticalFatalError: Boolean) {
        preferences.putBoolean(WAS_CRITICAL_FATAL_ERROR, wasCriticalFatalError)
    }

    override fun showReportDialogOnStart(show: Boolean) {
        preferences.putBoolean(SHOW_REPORT_DIALOG_ON_START, show)
    }

    override fun isReportDialogOnStartEnabled(): Boolean {
        return preferences.getBoolean(SHOW_REPORT_DIALOG_ON_START, true)
    }

    override fun clearErrorFlags() {
        setWasFatalError(false)
        setWasCriticalFatalError(false)
    }

    private companion object {
        const val PREFERENCES_NAME = "logger_preferences"
        const val WAS_FATAL_ERROR = "was_fatal_error"
        const val WAS_CRITICAL_FATAL_ERROR = "was_critical_fatal_error"
        const val SHOW_REPORT_DIALOG_ON_START = "show_report_dialog_on_start"
    }
}