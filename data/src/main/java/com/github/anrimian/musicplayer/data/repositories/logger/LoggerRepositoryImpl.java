package com.github.anrimian.musicplayer.data.repositories.logger;

import android.content.Context;

import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;

public class LoggerRepositoryImpl implements LoggerRepository {
    
    @interface Constants {
        String WAS_FATAL_ERROR = "was_fatal_error";
        String WAS_CRITICAL_FATAL_ERROR = "was_critical_fatal_error";
        String SHOW_REPORT_DIALOG_ON_START = "show_report_dialog_on_start";
    }
    
    private final Context context;

    public LoggerRepositoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public boolean wasFatalError() {
        return false;
    }

    @Override
    public void setWasFatalError(boolean wasFatalError) {

    }

    @Override
    public boolean wasCriticalFatalError() {
        return false;
    }

    @Override
    public void setWasCriticalFatalError(boolean wasCriticalFatalError) {

    }

    @Override
    public boolean showReportDialogOnStart(boolean show) {
        return false;
    }

    @Override
    public boolean isReportDialogOnStartEnabled() {
        return false;
    }

    @Override
    public void clearErrorFlags() {

    }
}
