package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser

import java.util.*

class PlayListFile(
    val name: String,
    val createDate: Date,
    val modifyDate: Date,
    val entries: List<PlayListEntry>
)