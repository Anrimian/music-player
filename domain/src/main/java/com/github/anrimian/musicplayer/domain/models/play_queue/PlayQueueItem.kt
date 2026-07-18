package com.github.anrimian.musicplayer.domain.models.play_queue

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

class PlayQueueItem(
    val itemId: Long,
    id: Long,
    title: String,
    artist: String?,
    album: String?,
    duration: Long,
    size: Long,
    comment: String?,
    storageId: Long?,
    addedTime: Long,
    modifiedTime: Long,
    coverModifyTime: Long,
    fileStatus: LocalFileStatus,
    corruptionType: CorruptionType?,
    isFileExists: Boolean,
    initialSource: InitialSource,
): Composition(
    id,
    title,
    artist,
    album,
    duration,
    size,
    comment,
    storageId,
    addedTime,
    modifiedTime,
    coverModifyTime,
    fileStatus,
    corruptionType,
    isFileExists,
    initialSource
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayQueueItem

        return itemId == other.itemId
    }

    override fun hashCode(): Int {
        return itemId.hashCode()
    }

    override fun toString(): String {
        return "PlayQueueItem(id=$itemId, composition=${super.toString()})"
    }


}