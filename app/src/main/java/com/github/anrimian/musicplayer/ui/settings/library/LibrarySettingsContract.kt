package com.github.anrimian.musicplayer.ui.settings.library

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Immutable
data class LibrarySettingsState(
    val isDoNotShowAppConfirmDialogEnabled: Boolean = false,
    val audioFileMinDurationMillis: Long = 0,
    val allowedFileExtensions: Set<String> = emptySet(),
    val playlistDuplicateCheckEnabled: Boolean = false,
    val playlistInsertStartEnabled: Boolean = false
)

sealed interface LibrarySettingsDialogs : AppDialog {
    @Parcelize
    data class SelectMinDurationDialog(val currentValue: Long) : LibrarySettingsDialogs
    @Parcelize
    data class ConfirmMinDurationChangeDialog(
        val filesToRemoveCount: Int,
        val newMillis: Long
    ) : LibrarySettingsDialogs
    @Parcelize
    data class EditAllowedExtensionsDialog(
        val extensions: Set<String>
    ) : LibrarySettingsDialogs
    @Parcelize
    data class ConfirmAllowedExtensionsChangeDialog(
        val filesToRemoveCount: Int,
        val newExtensions: Set<String>
    ) : LibrarySettingsDialogs
}

sealed interface LibrarySettingsEffect : BaseEffect