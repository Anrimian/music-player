package com.github.anrimian.musicplayer.data.storage.providers.playlists

class StoragePlaylist(
    val storageId: Long,
    val name: String,
    val addedTime: Long,
    val modifiedTime: Long
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoragePlaylist) return false

        if (storageId != other.storageId) return false

        return true
    }

    override fun hashCode(): Int {
        return storageId.hashCode()
    }

    override fun toString(): String {
        return "StoragePlayList(storageId=$storageId, name='$name', addedTime=$addedTime, modifiedTime=$modifiedTime)"
    }

}
