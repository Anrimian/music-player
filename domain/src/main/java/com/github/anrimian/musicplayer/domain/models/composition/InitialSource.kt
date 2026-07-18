package com.github.anrimian.musicplayer.domain.models.composition


enum class InitialSource(val id: Int) {
    LOCAL(1),
    REMOTE(2),
    APP(3);

    companion object {
        private val idMap = HashMap<Int, InitialSource>().apply {
            InitialSource.entries.forEach { source -> put(source.id, source) }
        }

        fun fromId(id: Int): InitialSource {
            return idMap[id] ?: throw IllegalStateException()
        }
    }

}