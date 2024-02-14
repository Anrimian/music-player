package com.github.anrimian.musicplayer.domain.models.composition

enum class CorruptionType {
    UNKNOWN,
    UNSUPPORTED,
    NOT_FOUND,
    SOURCE_NOT_FOUND,
    TOO_LARGE_SOURCE,
    FILE_IS_CORRUPTED,
    FILE_READ_TIMEOUT
}