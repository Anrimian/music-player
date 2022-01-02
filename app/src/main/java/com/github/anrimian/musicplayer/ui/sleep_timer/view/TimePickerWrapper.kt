package com.github.anrimian.musicplayer.ui.sleep_timer.view

import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import android.widget.TextView
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher.onTextChanged
import java.util.concurrent.TimeUnit
import kotlin.math.log10


class TimePickerWrapper(
        private val etSeconds: EditText,
        private val etMinutes: EditText,
        private val etHours: EditText,
        private val onTimePicked: (Long) -> Unit,
) {

    private var seconds = -1
    private var minutes = -1
    private var hours = -1

    init {
        setMinTimeTextWidth(etSeconds)
        etSeconds.filters = arrayOf(InputFilterMinMax(0, 59))
        etSeconds.onTimeTextChanged { seconds -> this.seconds = seconds }
        etSeconds.setOnEditorActionListener { _, _, _ ->
            etSeconds.clearFocus()
            return@setOnEditorActionListener false
        }

        setMinTimeTextWidth(etMinutes)
        etMinutes.filters = arrayOf(InputFilterMinMax(0, 59))
        etMinutes.onTimeTextChanged { minutes -> this.minutes = minutes }

        setMinTimeTextWidth(etHours)
        etHours.filters = arrayOf(InputFilterMinMax(0, 99))
        etHours.onTimeTextChanged { hours -> this.hours = hours }
    }

    fun showTime(millis: Long) {
        seconds = TimeUnit.MILLISECONDS.toSeconds(millis).toInt() % 60
        etSeconds.setText(String.format("%02d", seconds))

        minutes = TimeUnit.MILLISECONDS.toMinutes(millis).toInt() % 60
        etMinutes.setText(String.format("%02d", minutes))

        hours = TimeUnit.MILLISECONDS.toHours(millis).toInt()
        etHours.setText(String.format("%02d", hours))
    }

    fun setVisibility(visibility: Int) {
        etSeconds.visibility = visibility
        etMinutes.visibility = visibility
        etHours.visibility = visibility
    }

    fun setEnabled(enabled: Boolean) {
        etSeconds.isEnabled = enabled
        etSeconds.clearFocus()
        etMinutes.isEnabled = enabled
        etMinutes.clearFocus()
        etHours.isEnabled = enabled
        etHours.clearFocus()
    }

    private fun onTimePickerValueChanged() {
        val millis = ((((hours * 60) + minutes) * 60) + seconds) * 1000L
        onTimePicked(millis)
    }

    private fun setMinTimeTextWidth(view: TextView) {
        val measureText: Float = view.paint.measureText("00")
        view.width = view.paddingLeft + view.paddingRight + measureText.toInt()
    }

    private fun EditText.onTimeTextChanged(onChanged: (Int) -> Unit) {
        onTextChanged(this) { text ->
            try {
                val number = if (text.isEmpty()) 0 else text.toInt()
                onChanged(number)
                onTimePickerValueChanged()
            } catch (e: NumberFormatException) {}
        }
    }

    private class InputFilterMinMax(
            private val min: Int,
            private val max: Int,
    ) : InputFilter {

        private val maxLength = log10(max.toDouble()) + 1

        override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int,
        ): CharSequence? {
            try {
                val input = (dest.toString() + source.toString())
                if (input.length > maxLength) {
                    return ""
                }
                if (isInRange(min, max, input.toInt())) {
                    return null
                }
            } catch (nfe: NumberFormatException) {}
            return ""
        }

        private fun isInRange(min: Int, max: Int, number: Int): Boolean {
            return if (max > min) number in min..max else number in max..min
        }
    }
}