package com.github.anrimian.musicplayer.domain.models.folders

import com.github.anrimian.musicplayer.domain.models.composition.Composition

sealed interface FileSource

data class CompositionFileSource(val composition: Composition): FileSource

sealed interface AbstractDirectory : FileSource {
    fun getFolderId(): Long
}

class FolderFileSource(
    val id: Long,
    val name: String,
    val filesCount: Int,
    val hasAnyStorageFile: Boolean
) : AbstractDirectory {

    override fun getFolderId() = id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderFileSource

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class Volume(
    val id: Long,
    val rootFolderId: Long,
    val storageKey: String,
    val path: String,
    val compositionsCount: Int
) : AbstractDirectory {

    override fun getFolderId() = rootFolderId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Volume) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}