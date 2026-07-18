package com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser

import androidx.compose.runtime.Immutable

@Immutable
data class LyricsWord(
    val text: String,
    val timeStart: Long,
    val duration: Long,
    val charStartIndex: Int = 0,
    val charEndIndex: Int = 0,
    val trimmedCharEndIndex: Int = 0,
)
