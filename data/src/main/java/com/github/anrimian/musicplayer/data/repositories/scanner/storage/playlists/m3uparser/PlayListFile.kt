package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser

class PlayListFile(
    val name: String,
    val createDate: Long,
    val modifyDate: Long,
    val entries: List<PlayListEntry>
)