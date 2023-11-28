package com.github.anrimian.musicplayer.domain.models.composition

class DeletedComposition(
    val fileName: String,
    val parentPath: String,
    val storageId: Long?,
    val title: String
) {
    override fun toString(): String {
        return "DeletedComposition(fileName='$fileName', parentPath='$parentPath', storageId=$storageId, title='$title')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeletedComposition

        if (fileName != other.fileName) return false
        if (parentPath != other.parentPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + parentPath.hashCode()
        return result
    }


}