package com.github.anrimian.musicplayer.utils.system

import android.os.Process
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils

class SystemCrashExceptionHandler(
    private val nextHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, e: Throwable) {
        if (AndroidUtils.isDeadSystemException(e)) {
            // Exit silently. The system server is dead and our process is scheduled for termination.
            // Exiting silently prevents reporting a false-positive fatal application crash.
            Process.killProcess(Process.myPid())
            System.exit(10)
            return
        }
        nextHandler?.uncaughtException(thread, e)
    }

    companion object {
        @JvmStatic
        fun init() {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(SystemCrashExceptionHandler(defaultHandler))
        }
    }
}