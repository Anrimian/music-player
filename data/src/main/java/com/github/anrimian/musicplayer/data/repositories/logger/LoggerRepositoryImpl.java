package com.github.anrimian.musicplayer.data.repositories.logger;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;

import static com.github.anrimian.musicplayer.data.repositories.logger.LoggerRepositoryImpl.Constants.PREFERENCES_NAME;
import static com.github.anrimian.musicplayer.data.repositories.logger.LoggerRepositoryImpl.Constants.SHOW_REPORT_DIALOG_ON_START;
import static com.github.anrimian.musicplayer.data.repositories.logger.LoggerRepositoryImpl.Constants.WAS_CRITICAL_FATAL_ERROR;
import static com.github.anrimian.musicplayer.data.repositories.logger.LoggerRepositoryImpl.Constants.WAS_FATAL_ERROR;

public class LoggerRepositoryImpl implements LoggerRepository {
    
    interface Constants {
        String PREFERENCES_NAME = "logger_preferences";

        String WAS_FATAL_ERROR = "was_fatal_error";
        String WAS_CRITICAL_FATAL_ERROR = "was_critical_fatal_error";
        String SHOW_REPORT_DIALOG_ON_START = "show_report_dialog_on_start";
    }
    
    private final SharedPreferencesHelper preferences;

    public LoggerRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public boolean wasFatalError() {
        return preferences.getBoolean(WAS_FATAL_ERROR, false);
    }

    @Override
    public void setWasFatalError(boolean wasFatalError) {
        preferences.putBoolean(WAS_FATAL_ERROR, wasFatalError);
    }

    @Override
    public boolean wasCriticalFatalError() {
        return preferences.getBoolean(WAS_CRITICAL_FATAL_ERROR, false);
    }

    @Override
    public void setWasCriticalFatalError(boolean wasCriticalFatalError) {
        preferences.putBoolean(WAS_CRITICAL_FATAL_ERROR, wasCriticalFatalError);
    }

    @Override
    public void showReportDialogOnStart(boolean show) {
        preferences.putBoolean(SHOW_REPORT_DIALOG_ON_START, show);
    }

    @Override
    public boolean isReportDialogOnStartEnabled() {
        return preferences.getBoolean(SHOW_REPORT_DIALOG_ON_START, true);
    }

    @Override
    public void clearErrorFlags() {
        setWasFatalError(false);
        setWasCriticalFatalError(false);
    }
}
