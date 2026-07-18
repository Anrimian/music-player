package com.github.anrimian.musicplayer.utils.logger

import android.app.Activity
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository
import com.github.anrimian.musicplayer.ui.utils.getAppInfo
import com.github.anrimian.musicplayer.utils.ExternalAppIntents.startEmailSendFileScreen
import com.github.anrimian.musicplayer.utils.ExternalAppIntents.startViewTextFileScreen

class AppLogger(
    private val fileLog: FileLog,
    private val loggerRepository: LoggerRepository
) {

    fun initFatalErrorRecorder() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            loggerRepository.setWasFatalError(true)
            fileLog.writeFatalException(e)
            oldHandler?.uncaughtException(thread, e)
        }
    }

    fun startViewLogScreen(activity: Activity) {
        fileLog.getFile().startViewTextFileScreen(activity)
    }

    fun startSendLogScreen(activity: Activity) {
        val appInfo = activity.getAppInfo()
        val subject = "Log info(v: ${appInfo.versionName}, build: ${appInfo.versionCode})"
        val email = activity.getString(R.string.log_email)
        val chooserTitle = activity.getString(R.string.pick_email_app_to_send)

        fileLog.getFile().startEmailSendFileScreen(activity, subject, email, chooserTitle)
    }

}