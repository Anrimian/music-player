
package com.github.anrimian.musicplayer.wear.ui.utils;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.provider.Settings
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.os.ConfigurationCompat
import androidx.wear.widget.CurvedTextView
import java.util.Calendar


class TimeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : CurvedTextView(context, attrs, defStyleAttr, defStyleRes) {

    private var time = Calendar.getInstance()

    private val timeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIMEZONE_CHANGED -> onTimeZoneChange()
                Intent.ACTION_TIME_TICK, Intent.ACTION_TIME_CHANGED -> updateTimeText()
            }
        }
    }

    private val timeContentObserver by lazy(LazyThreadSafetyMode.NONE) {
        object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                updateTimeText()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateTimeText()

        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.TIME_12_24),
            true,
            timeContentObserver
        )
        context.registerReceiver(timeBroadcastReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        context.contentResolver.unregisterContentObserver(timeContentObserver)
        context.unregisterReceiver(timeBroadcastReceiver)
    }

    private fun updateTimeText() {
        val pattern = DateFormat.getBestDateTimePattern(
            ConfigurationCompat.getLocales(resources.configuration)[0],
            if (DateFormat.is24HourFormat(context)) "Hm" else "hm"
        )
        // Remove the am/pm indicator (if any). This is locale safe.
        val patternWithoutAmPm = pattern.replace("a", "").trim()

        time.timeInMillis = System.currentTimeMillis()
        text = DateFormat.format(patternWithoutAmPm, time).toString()
    }

    private fun onTimeZoneChange() {
        time = Calendar.getInstance()
        updateTimeText()
    }
}