package com.github.anrimian.musicplayer.domain.models.artist

class Artist(
    val id: Long,
    val name: String,
    val compositionsCount: Int,
    val albumsCount: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Artist

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}