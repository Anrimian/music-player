package com.github.anrimian.musicplayer.ui.utils.compose

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

fun HapticFeedback.performGestureThresholdActivate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val type = HapticFeedbackType(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE)
        performHapticFeedback(type)
    }
}