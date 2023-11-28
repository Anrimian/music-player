package com.github.anrimian.musicplayer.data.models.exceptions

class PlaylistNotCompletelyImportedException(
    val playlistId: Long,
    val notFoundFilesCount: Int
): RuntimeException()