package com.github.anrimian.musicplayer.domain.models.folders

class FolderInfo(
    val id: Long,
    val path: String,
    val isParentOfParentRoot: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderInfo

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}