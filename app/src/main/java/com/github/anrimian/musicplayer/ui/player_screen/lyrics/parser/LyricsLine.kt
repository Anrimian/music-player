package com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser

import androidx.compose.runtime.Immutable

@Immutable
data class LyricsLine(
    val words: List<LyricsWord>,
    val timeStart: Long,
    val duration: Long,
    val roleIndex: Int = 0,
)
