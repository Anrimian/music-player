package com.github.anrimian.musicplayer.data.models.composition

class ExternalComposition(
    val parentPath: String,
    val fileName: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val genres: String?,
    val trackNumber: Long?,
    val discNumber: Long?,
    val comment: String?,
    val lyrics: String?,
    val duration: Long,
    val size: Long,
    val addedTime: Long,
    val modifiedTime: Long,
    val pathModifyTime: Long?,
    val missingTime: Long,
    val coverModifyTime: Long,
    val isFileExists: Boolean
)
