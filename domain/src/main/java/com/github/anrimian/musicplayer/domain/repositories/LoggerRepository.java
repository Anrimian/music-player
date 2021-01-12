package com.github.anrimian.musicplayer.domain.repositories;

//TODO implement report dialog
//TODO try to fix report intent action
public interface LoggerRepository {

    boolean wasFatalError();

    void setWasFatalError(boolean wasFatalError);

    boolean wasCriticalFatalError();

    /**
     * Set this flag when app is unable to start
     * Do not start screens when this flag is set
     * Ignore flag about "do not show again" when this flag is set
     */
    void setWasCriticalFatalError(boolean wasCriticalFatalError);

    void showReportDialogOnStart(boolean show);

    boolean isReportDialogOnStartEnabled();

    void clearErrorFlags();
}
