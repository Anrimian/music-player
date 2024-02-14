package com.github.anrimian.musicplayer.domain.models.albums

class Album(
    val id: Long,
    val name: String,
    val artist: String?,
    val compositionsCount: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Album

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}