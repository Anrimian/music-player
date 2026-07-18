package com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser

import androidx.compose.runtime.Immutable

@Immutable
data class FocusLyricsPart(
    val lineIndex: Int,
    val wordIndex: Int,
    val isFocused: Boolean
)
