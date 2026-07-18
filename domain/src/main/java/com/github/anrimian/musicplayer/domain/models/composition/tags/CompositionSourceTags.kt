package com.github.anrimian.musicplayer.domain.models.composition.tags

class CompositionSourceTags(
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val durationSeconds: Int,
    val trackNumber: Long?,
    val discNumber: Long?,
    val comment: String?,
    val lyrics: String?,
    val genres: Array<String>
)
