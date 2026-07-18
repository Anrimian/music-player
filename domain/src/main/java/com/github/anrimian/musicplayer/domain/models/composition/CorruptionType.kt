package com.github.anrimian.musicplayer.domain.models.composition

enum class CorruptionType(val id: Int) {
    UNKNOWN(1),
    UNSUPPORTED(2),
    NOT_FOUND(3),
    NOT_FOUND_IN_ALL_STORAGES(4),
    TOO_LARGE_SOURCE(5),
    FILE_IS_CORRUPTED(6),
    FILE_READ_TIMEOUT(7),
    NOT_ALLOWED_PATH(8);

    companion object {
        private val idMap = HashMap<Int, CorruptionType>().apply {
            CorruptionType.entries.forEach { source -> put(source.id, source) }
        }

        fun fromId(id: Int): CorruptionType {
            return idMap[id] ?: throw IllegalStateException()
        }
    }

}