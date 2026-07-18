package com.github.anrimian.musicplayer.domain.models.albums

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

class AlbumComposition(
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
    val trackNumber: Long?,
    val discNumber: Long?
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
)