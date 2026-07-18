package com.github.anrimian.musicplayer.ui.common.models.menu

import androidx.annotation.DrawableRes
import com.github.anrimian.musicplayer.ui.utils.compose.UiText

data class AppMenuItem(
    val id: Int,
    val title: UiText,
    @param:DrawableRes val iconRes: Int? = null,
    val groupId: Int = 0,
)