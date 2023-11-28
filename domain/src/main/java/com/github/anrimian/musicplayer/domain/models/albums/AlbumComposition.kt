package com.github.anrimian.musicplayer.domain.models.albums

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import java.util.Date

class AlbumComposition(
    artist: String?,
    title: String,
    album: String?,
    duration: Long,
    size: Long,
    id: Long,
    storageId: Long?,
    dateAdded: Date,
    dateModified: Date,
    coverModifyTime: Date,
    corruptionType: CorruptionType?,
    isFileExists: Boolean,
    initialSource: InitialSource,
    val trackNumber: Long?,
    val discNumber: Long?
): Composition(
    artist,
    title,
    album,
    duration,
    size,
    id,
    storageId,
    dateAdded,
    dateModified,
    coverModifyTime,
    corruptionType,
    isFileExists,
    initialSource
)