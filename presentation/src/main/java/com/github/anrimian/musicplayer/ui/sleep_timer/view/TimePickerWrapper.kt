package com.github.anrimian.musicplayer.ui.sleep_timer.view

import android.widget.NumberPicker
import java.util.concurrent.TimeUnit

class TimePickerWrapper(
        private val secondsPicker: NumberPicker,
        private val minutesPicker: NumberPicker,
        private val hoursPicker: NumberPicker,
        private val onTimePicked: (Long) -> Unit,
) {

    private var seconds = 0
    private var minutes = 0
    private var hours = 0
    
    init {
        secondsPicker.minValue = 0
        secondsPicker.maxValue = 59
        secondsPicker.setFormatter { i -> String.format("%02d", i) }
        secondsPicker.setOnValueChangedListener { _, _, newValue ->
            seconds = newValue
            onTimePickerValueChanged()
        }

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.setFormatter { i -> String.format("%02d", i) }
        minutesPicker.setOnValueChangedListener { _, _, newValue ->
            minutes = newValue
            onTimePickerValueChanged()
        }

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 99
        hoursPicker.setFormatter { i -> String.format("%02d", i) }
        hoursPicker.setOnValueChangedListener { _, _, newValue ->
            hours = newValue
            onTimePickerValueChanged()
        }
    }

    fun showTime(millis: Long) {
        seconds = TimeUnit.MILLISECONDS.toSeconds(millis).toInt() % 60
        secondsPicker.value = seconds

        minutes = TimeUnit.MILLISECONDS.toMinutes(millis).toInt() % 60
        minutesPicker.value = minutes

        hours = TimeUnit.MILLISECONDS.toHours(millis).toInt()
        hoursPicker.value = hours
    }

    fun setVisibility(visibility: Int) {
        secondsPicker.visibility = visibility
        minutesPicker.visibility = visibility
        hoursPicker.visibility = visibility
    }

    fun setEnabled(enabled: Boolean) {
        secondsPicker.isEnabled = enabled
        minutesPicker.isEnabled = enabled
        hoursPicker.isEnabled = enabled
    }

    private fun onTimePickerValueChanged() {
        val millis = ((((hours * 60) + minutes) * 60) + seconds) * 1000L
        onTimePicked(millis)
    }
}