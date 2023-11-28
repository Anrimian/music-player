package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser

class PlayListEntry(
    val filePath: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayListEntry

        if (filePath != other.filePath) return false

        return true
    }

    override fun hashCode(): Int {
        return filePath.hashCode()
    }

    override fun toString(): String {
        return "PlayListEntry(filePath='$filePath')"
    }
}