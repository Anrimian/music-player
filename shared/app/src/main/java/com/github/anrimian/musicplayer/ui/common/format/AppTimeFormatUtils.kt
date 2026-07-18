package com.github.anrimian.musicplayer.ui.common.format

import java.util.Locale
import java.util.concurrent.TimeUnit

object AppTimeFormatUtils {

    fun formatMilliseconds(millis: Long, cutZeroNumbers: Boolean = true): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))

        val sb = StringBuilder()

        if (hours != 0L || !cutZeroNumbers) {
            sb.append(String.format(Locale.getDefault(), "%02d", hours))
            sb.append(":")
        }

        if (minutes != 0L || hours != 0L || !cutZeroNumbers) {
            sb.append(String.format(Locale.getDefault(), "%02d", minutes))
        } else {
            sb.append("00")
        }
        sb.append(":")

        sb.append(String.format(Locale.getDefault(), "%02d", seconds))
        return sb.toString()
    }
}