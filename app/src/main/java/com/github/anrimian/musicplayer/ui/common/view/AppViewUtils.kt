package com.github.anrimian.musicplayer.ui.common.view

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.format.getHighlightColor
import com.github.anrimian.musicplayer.ui.common.onHold
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.RepeatListener
import com.github.anrimian.musicplayer.ui.utils.attachSystemBarsColor
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.isLandscape
import com.github.anrimian.musicplayer.ui.utils.isTablet
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

fun Activity.attachSystemBarsColor() {
    val navBarBackground = if (!isTablet() && isLandscape()) R.attr.horizontalNavBarBackground else R.attr.playerPanelBackground
    attachSystemBarsColor(attrColor(navBarBackground), attrColor(R.attr.toolbarColor))
}

fun View.onLongVibrationClick(onClick: () -> Unit) {
    setOnLongClickListener {
        AndroidUtils.playShortVibration(context)
        onClick()
        true
    }
}
