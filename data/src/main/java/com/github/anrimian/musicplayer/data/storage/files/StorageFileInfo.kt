package com.github.anrimian.musicplayer.data.storage.files

class StorageFileInfo(
    val id: Long,
    val storageId: Long,
    val fileName: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StorageFileInfo) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "StorageAudioFileInfo(id=$id, storageId=$storageId, fileName='$fileName')"
    }

}