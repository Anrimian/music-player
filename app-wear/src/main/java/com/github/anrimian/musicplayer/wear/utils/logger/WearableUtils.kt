package com.github.anrimian.musicplayer.wear.utils.logger

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.getSystemService
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import kotlin.math.abs

fun View.onRotaryInputChanged(rotateThreshold: Float = 32f, onRotated: (Float) -> Unit) {
    var rotatedValue = 0f
    setOnGenericMotionListener { _: View, event: MotionEvent ->
        if (event.action != MotionEvent.ACTION_SCROLL || !event.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)) {
            return@setOnGenericMotionListener false
        }
        val axisValue = event.getAxisValue(MotionEventCompat.AXIS_SCROLL)
        val scrollFactor = ViewConfigurationCompat.getScaledVerticalScrollFactor(ViewConfiguration.get(context), context)
        val delta = axisValue * scrollFactor
        rotatedValue += delta
        if (abs(rotatedValue) < rotateThreshold) {
            return@setOnGenericMotionListener false
        }
        onRotated(rotatedValue)
        rotatedValue = 0f
        return@setOnGenericMotionListener true

    }
}

fun Context.playTickVibration() {
    val v: Vibrator = getSystemService<Vibrator>() ?: return
    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
}