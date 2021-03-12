package com.github.anrimian.musicplayer.ui.sleep_timer.view

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import java.util.concurrent.TimeUnit


class TimePickerWrapper2(
        private val secondsPicker: NumberInputView,
        private val minutesPicker: EditText,
        private val hoursPicker: EditText,
        private val onTimePicked: (Long) -> Unit,
) {

    private var seconds = 0
    private var minutes = 0
    private var hours = 0

    init {
        secondsPicker.filters = arrayOf(InputFilterMinMax(0, 59))
        secondsPicker.textListener = { number ->
            seconds = number
            onTimePickerValueChanged()
        }



//        secondsPicker.setFormatter { i -> String.format("%02d", i) }
//        secondsPicker.setOnValueChangedListener { _, _, newValue ->
//            seconds = newValue
//            onTimePickerValueChanged()
//        }

//        minutesPicker.filters = arrayOf(InputFilterMinMax(0, 59))
        minutesPicker.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
//                if (hasFocus) {
//                    minutesPicker.setSelection(0)
//                }
            }
        }
        minutesPicker.addTextChangedListener(object : TextWatcher {
            private var position = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (minutesPicker.isFocused) {
//                    val changed = s[start]
//                    Log.d("KEK", "onTextChanged: $changed")
                }

                val text = String.format("%02d", safeGetNumber(s))
                if (text != s.toString()) {
                    minutesPicker.setText(text)
                }

            }

            override fun afterTextChanged(s: Editable) {

            }
        })
//        minutesPicker.setFormatter { i -> String.format("%02d", i) }
//        minutesPicker.setOnValueChangedListener { _, _, newValue ->
//            minutes = newValue
//            onTimePickerValueChanged()
//        }

        hoursPicker.filters = arrayOf(InputFilterMinMax(0, 99))
//        hoursPicker.setFormatter { i -> String.format("%02d", i) }
//        hoursPicker.setOnValueChangedListener { _, _, newValue ->
//            hours = newValue
//            onTimePickerValueChanged()
//        }
    }

    fun showTime(millis: Long) {
        seconds = TimeUnit.MILLISECONDS.toSeconds(millis).toInt() % 60
        secondsPicker.setText(seconds.toString())

        minutes = TimeUnit.MILLISECONDS.toMinutes(millis).toInt() % 60
        minutesPicker.setText(minutes.toString())

        hours = TimeUnit.MILLISECONDS.toHours(millis).toInt()
        hoursPicker.setText(hours.toString())
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

    private fun safeGetNumber(editable: CharSequence) = try {
        editable.toString().toInt()
    } catch (e: NumberFormatException) {
        0
    }

    private class NumberFormatInputFilter : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
            return String.format("%02d", safeGetNumber(source))
        }

        private fun safeGetNumber(editable: CharSequence) = try {
            editable.toString().toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    private class InputFilterMinMax(
            private val min: Int,
            private val max: Int
    ) : InputFilter {

        override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
        ): CharSequence? {
            try {
                val input = (dest.toString() + source.toString()).toInt()
                if (isInRange(min, max, input)) {
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