package com.github.anrimian.musicplayer.ui.common.view

import android.view.View
import com.github.anrimian.musicplayer.ui.common.format.getHighlightColor
import com.github.anrimian.musicplayer.ui.utils.runHighlightAnimation

fun View.runHighlightAnimation() {
    val color = context.getHighlightColor()
    runHighlightAnimation(color)
}
