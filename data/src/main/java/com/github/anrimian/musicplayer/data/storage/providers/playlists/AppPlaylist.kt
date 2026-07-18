package com.github.anrimian.musicplayer.data.storage.providers.playlists

class AppPlaylist(
    val id: Long,
    val storageId: Long,
    val name: String,
    val addedTime: Long,
    val modifiedTime: Long,
    val compositionsCount: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppPlaylist) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "AppPlayList(id=$id, storageId=$storageId, name='$name', addedTime=$addedTime, modifiedTime=$modifiedTime, compositionsCount=$compositionsCount)"
    }

}
