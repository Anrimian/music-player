package com.github.anrimian.musicplayer.domain.models.composition

/**
 * Created on 24.10.2017.
 */
open class Composition(
    override val id: Long,
    override val title: String,
    override val artist: String?,
    override val album: String?,
    override val duration: Long,
    override val size: Long,
    override val comment: String?,
    override val storageId: Long?,
    override val addedTime: Long,
    override val modifiedTime: Long,
    override val coverModifyTime: Long,
    override val fileStatus: LocalFileStatus,
    override val corruptionType: CorruptionType?,
    override val isFileExists: Boolean,
    override val initialSource: InitialSource
) : CompositionModel {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Composition) return false //compare child classes too
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Composition(title='$title', id=$id)"
    }

}