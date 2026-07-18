package com.github.anrimian.musicplayer.ui.common

import android.view.View
import com.github.anrimian.musicplayer.ui.utils.RepeatListener

fun View.onHold(
    holdActionStartMillis: Int,
    holdActionIntervalMillis: Int,
    callCountToIncreaseSpeed: Int,
    startAction: () -> Unit,
    action: () -> Unit
) {
    if (!hasOnClickListeners()) {
        isClickable = true
    }
    setOnTouchListener(
        RepeatListener(
        holdActionStartMillis,
        holdActionIntervalMillis,
        callCountToIncreaseSpeed,
        startAction,
        action
    )
    )
}