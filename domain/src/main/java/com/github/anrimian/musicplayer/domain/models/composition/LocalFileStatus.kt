package com.github.anrimian.musicplayer.domain.models.composition

enum class LocalFileStatus(val id: Int) {
    AVAILABLE(1),
    DISAPPEARED(2),
    LIBRARY_ENTRY_ONLY(3);

    companion object {
        private val idMap = HashMap<Int, LocalFileStatus>().apply {
            LocalFileStatus.entries.forEach { source -> put(source.id, source) }
        }

        fun fromId(id: Int): LocalFileStatus {
            return idMap[id] ?: throw IllegalStateException()
        }
    }

}