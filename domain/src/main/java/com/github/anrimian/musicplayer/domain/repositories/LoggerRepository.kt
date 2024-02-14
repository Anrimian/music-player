package com.github.anrimian.musicplayer.domain.repositories

interface LoggerRepository {

    fun wasFatalError(): Boolean

    fun setWasFatalError(wasFatalError: Boolean)

    fun wasCriticalFatalError(): Boolean

    /**
     * Set this flag when app is unable to start
     * Do not start screens when this flag is set
     * Ignore flag about "do not show again" when this flag is set
     */
    fun setWasCriticalFatalError(wasCriticalFatalError: Boolean)

    fun showReportDialogOnStart(show: Boolean)

    fun isReportDialogOnStartEnabled(): Boolean

    fun clearErrorFlags()

}