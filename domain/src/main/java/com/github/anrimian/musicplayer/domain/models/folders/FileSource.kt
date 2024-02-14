package com.github.anrimian.musicplayer.domain.models.folders

import com.github.anrimian.musicplayer.domain.models.composition.Composition

sealed interface FileSource

data class CompositionFileSource(val composition: Composition): FileSource

class FolderFileSource(
    val id: Long,
    val name: String,
    val filesCount: Int,
    val hasAnyStorageFile: Boolean
) : FileSource {
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