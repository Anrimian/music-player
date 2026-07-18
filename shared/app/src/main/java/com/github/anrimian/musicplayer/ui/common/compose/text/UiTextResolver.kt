package com.github.anrimian.musicplayer.ui.common.compose.text

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.github.anrimian.musicplayer.ui.utils.compose.UiText

class UiTextResolver(private val context: Context) {

    fun getString(uiText: UiText): String = uiText.resolve(context)

    fun UiText.resolve(): String = getString(this)
}

val LocalUiTextResolver = staticCompositionLocalOf<UiTextResolver> {
    error("No Resolver provided")
}