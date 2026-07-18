package com.github.anrimian.musicplayer.domain.models.playlist

data class Playlist(
    val id: Long,
    val name: String,
    val addedTime: Long,
    val modifiedTime: Long,
    val compositionsCount: Int,
    val totalDuration: Long
)
