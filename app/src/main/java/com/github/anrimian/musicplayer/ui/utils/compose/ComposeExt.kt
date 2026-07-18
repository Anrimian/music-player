package com.github.anrimian.musicplayer.ui.utils.compose

import androidx.annotation.AttrRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils

@Composable
fun attrColor(@AttrRes id: Int): Color {
    val context = LocalContext.current
    return Color(AndroidUtils.getColorFromAttr(context, id))
}