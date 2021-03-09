package com.github.anrimian.musicplayer.ui.sleep_timer.view

import android.widget.NumberPicker
import java.util.concurrent.TimeUnit

class TimePickerWrapper(
        private val secondsPicker: NumberPicker,
        private val minutesPicker: NumberPicker,
        private val hoursPicker: NumberPicker,
        private val onTimePicked: (Long) -> Unit
) {

    private var seconds = 0
    private var minutes = 0
    private var hours = 0
    
    init {
        secondsPicker.minValue = 0
        secondsPicker.maxValue = 60
        secondsPicker.setOnValueChangedListener { _, _, newValue ->
            seconds = newValue
            onTimePickerValueChanged()
        }

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 60
        minutesPicker.setOnValueChangedListener { _, _, newValue ->
            minutes = newValue
            onTimePickerValueChanged()
        }

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 100
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

        hours = TimeUnit.MILLISECONDS.toHours(millis).toInt() % 60
        hoursPicker.value = hours
    }
    
    private fun onTimePickerValueChanged() {
        val millis = ((((hours * 60) + minutes) * 60) + seconds) * 1000L
        onTimePicked(millis)
    }
}