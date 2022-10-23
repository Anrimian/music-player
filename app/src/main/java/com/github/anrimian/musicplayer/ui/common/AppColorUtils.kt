package com.github.anrimian.musicplayer.ui.common

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr

@ColorInt
fun Context.getNavigationViewPrimaryColorLight(): Int {
    val colorPrimary = colorFromAttr(R.attr.navigationViewPrimaryColor)
    return ColorUtils.blendARGB(colorPrimary, Color.WHITE, 0.3f)
}