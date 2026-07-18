package com.github.anrimian.domain.models

sealed class WearableComposition(
    val title: String,
    val artist: String?,
    val duration: Long
)

class LibraryWearableComposition(
    val id: Long,
    title: String,
    artist: String?,
    duration: Long
): WearableComposition(title, artist, duration)

class ExternalWearableComposition(
    title: String,
    artist: String?,
    duration: Long
): WearableComposition(title, artist, duration)