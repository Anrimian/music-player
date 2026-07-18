package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.FocusLyricsPart
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.LyricsText

@Immutable
data class LyricsState(
    val lyrics: StatedData<LyricsText> = StatedData.Loading,
    val currentLyricsPart: FocusLyricsPart? = null,
    val isEditLyricsEnabled: Boolean = false,
    val currentCompositionId: Long? = null
)

sealed interface LyricsEffect : BaseEffect {
    data object ShowSleepTimerDialog : LyricsEffect
    data object ShowEqualizerDialog : LyricsEffect
}
