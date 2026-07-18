package com.github.anrimian.musicplayer.domain.models.search

class CompositionLookup(
    val minDuration: Long? = null,
    val maxDuration: Long? = null,
    val fileExtensions: Set<String>? = null
)
