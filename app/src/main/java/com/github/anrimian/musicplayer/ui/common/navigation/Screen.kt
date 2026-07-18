package com.github.anrimian.musicplayer.ui.common.navigation

import kotlinx.parcelize.Parcelize

sealed class Screen : BaseScreen {
    @Parcelize
    data class PlaylistDetails(val playlistId: Long) : Screen()
    @Parcelize
    data class TagsEditor(val compositionId: Long) : Screen()
    @Parcelize
    data class LyricsEditor(val compositionId: Long) : Screen()
    @Parcelize
    data object DisplaySettings : Screen()
    @Parcelize
    data object LibrarySettings : Screen()
    @Parcelize
    data object PlayerSettings : Screen()
    @Parcelize
    data object HeadsetSettings : Screen()
    @Parcelize
    data object ThemeSettings : Screen()
    @Parcelize
    data object ExcludedFolders : Screen()
}