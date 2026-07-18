package com.github.anrimian.musicplayer.wear.infrastructure.analytics

import android.util.Log
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.wear.utils.logger.WearableFileLogger

//TODO-W debug impl with logs
class WearableAnalytics(private val wearableFileLogger: WearableFileLogger): Analytics {

    override fun processNonFatalError(throwable: Throwable) {
        processNonFatalError(throwable, null)
    }

    override fun processNonFatalError(throwable: Throwable, message: String?) {
        if (message != null) {
            Log.d("UNEXPECTED", message)
        }
        throwable.printStackTrace()
        wearableFileLogger.writeException(throwable, message)
    }

    override fun logMessage(message: String) {
        Log.d("UNEXPECTED", message)
        wearableFileLogger.writeMessage(message)
    }
}