package com.github.anrimian.musicplayer.data.storage.providers.music

import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

/**
 * Created on 24.10.2017.
 */
class DBComposition(
    val artist: String?,
    val albumArtist: String?,
    val title: String?,
    val fileName: String,
    val album: String?,
    val parentPath: String,
    val duration: Long,
    val size: Long,
    val id: Long,
    val storageId: Long,
    val folderId: Long?,
    val storageModifyTime: Long,
    val lastScanTime: Long,
    val missingTime: Long,
    val pathModifyTime: Long,
    val localFileStatus: LocalFileStatus,
    val initialSource: InitialSource
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DBComposition

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
