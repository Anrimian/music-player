package com.github.anrimian.musicplayer.domain.models.composition

private val idMap = HashMap<Int, InitialSource>().apply {
    InitialSource.values().forEach { source -> put(source.id, source) }
}

fun fromId(id: Int): InitialSource {
    return idMap[id] ?: throw IllegalStateException()
}

enum class InitialSource(val id: Int) {
    LOCAL(1),
    REMOTE(2),
    APP(3)
}