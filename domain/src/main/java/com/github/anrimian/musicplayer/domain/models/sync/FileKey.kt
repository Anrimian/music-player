package com.github.anrimian.musicplayer.domain.models.sync

import com.github.anrimian.musicplayer.domain.utils.normalize

open class FileKey(
    val name: String,
    val parentPath: String
) {

    private val normalizedName = normalize(name)
    private val normalizedParentPath = normalize(parentPath)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileKey) return false

        if (normalizedName != other.normalizedName) return false
        if (normalizedParentPath != other.normalizedParentPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = normalizedName.hashCode()
        result = 31 * result + normalizedParentPath.hashCode()
        return result
    }

    override fun toString(): String {
        return "FileKey(name='$name', parentPath='$parentPath')"
    }

}