package com.github.anrimian.musicplayer.domain.models.composition

import java.util.Date

/**
 * Created on 24.10.2017.
 */
open class Composition(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val size: Long,
    val comment: String?,
    val storageId: Long?,
    val dateAdded: Date,
    val dateModified: Date,
    val coverModifyTime: Date,
    val corruptionType: CorruptionType?,
    val isFileExists: Boolean,
    val initialSource: InitialSource
) {

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