package com.github.anrimian.musicplayer.data.storage.providers.music

/**
 * Created on 24.10.2017.
 */
class StorageAudioFile(
    val artist: String?,
    val title: String?,
    val fileName: String,
    var parentPath: String,
    val duration: Long,
    val size: Long,
    val storageId: Long,
    val addedTime: Long,
    val modifiedTime: Long,
) {

    fun createKey(): AudioFileKey {
        return AudioFileKey(parentPath, fileName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorageAudioFile

        return storageId == other.storageId
    }

    override fun hashCode(): Int {
        return storageId.hashCode()
    }

}
