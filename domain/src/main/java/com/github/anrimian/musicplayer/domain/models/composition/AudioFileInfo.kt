package com.github.anrimian.musicplayer.domain.models.composition

class AudioFileInfo(
    val id: Long,
    val fileName: String,
    val parentPath: String,
    val storageId: Long?,
    val pathModifyTime: Long?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioFileInfo) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun getPath() = "$parentPath/$fileName"
}