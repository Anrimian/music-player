package com.github.anrimian.musicplayer.ui.library.common.library

import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistDuplicateEntryDialog(
    val playlistName: String,
    val topDuplicateTitles: List<String>,
    val totalDuplicatesCount: Int,
    val hasNonDuplicates: Boolean,
    val isDuplicateCheckEnabled: Boolean
) : AppDialog