package com.github.anrimian.musicplayer.ui.settings.main

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect

@Immutable
data class SettingsState(
    val missingCompositionsCount: Int = 0
)

sealed interface SettingsEffect : BaseEffect {
    data object OpenMissingFilesDialog : SettingsEffect
}