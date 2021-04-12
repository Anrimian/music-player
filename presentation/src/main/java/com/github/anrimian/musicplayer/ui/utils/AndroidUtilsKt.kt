package com.github.anrimian.musicplayer.ui.utils

import android.content.Context
import androidx.annotation.DimenRes

fun Context.getDimensionPixelSize(@DimenRes resId: Int): Int {
    return resources.getDimensionPixelSize(resId)
}
