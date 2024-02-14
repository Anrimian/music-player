package com.github.anrimian.musicplayer.ui.common.view

import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.format.getHighlightColor
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.RepeatListener
import com.github.anrimian.musicplayer.ui.utils.runHighlightAnimation

fun View.runHighlightAnimation() {
    val color = context.getHighlightColor()
    runHighlightAnimation(color)
}

fun View.onRewindHold(action: () -> Unit) {
    onHold(
        500,
        400,
        5,
        { AndroidUtils.playShortVibration(context) },
        action
    )
}

fun View.onVolumeHold(action: () -> Unit) {
    onHold(50, 80, Int.MAX_VALUE, {}, action)
}

fun View.onSpeedHold(action: () -> Unit) {
    onHold(50, 160, Int.MAX_VALUE, {}, action)
}

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
    setOnTouchListener(RepeatListener(
        holdActionStartMillis,
        holdActionIntervalMillis,
        callCountToIncreaseSpeed,
        startAction,
        action
    ))
}

fun TextView.setSmallDrawableStart(@DrawableRes drawableRes: Int) {
    val icon = ContextCompat.getDrawable(context, drawableRes)!!
    val resources = context.resources
    val iconSize = resources.getDimensionPixelSize(R.dimen.panel_secondary_icon_size)
    icon.setBounds(0, 0, iconSize, iconSize)
    this.setCompoundDrawables(icon, null, null, null)
    TextViewCompat.setCompoundDrawableTintList(
        this,
        ContextCompat.getColorStateList(context, R.color.color_text_secondary)
    )
    val iconPadding = resources.getDimensionPixelSize(R.dimen.panel_secondary_icon_padding)
    this.compoundDrawablePadding = iconPadding
}
