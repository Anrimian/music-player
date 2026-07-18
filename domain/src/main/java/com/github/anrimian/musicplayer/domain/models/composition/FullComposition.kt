package com.github.anrimian.musicplayer.domain.models.composition

/**
 * Created on 24.10.2017.
 */
class FullComposition(
    val artist: String?,
    val title: String?,
    val album: String?,
    val albumArtist: String?,
    val trackNumber: Long?,
    val discNumber: Long?,
    val comment: String?,
    val lyrics: String?,
    val fileName: String,
    val genres: String?,
    val duration: Long,
    val size: Long,
    val id: Long,
    val storageId: Long?,
    val addedTime: Long,
    val modifiedTime: Long,
    val coverModifyTime: Long,
    val fileStatus: LocalFileStatus,
    val corruptionType: CorruptionType?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FullComposition

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "FullComposition(fileName='$fileName', id=$id)"
    }

}