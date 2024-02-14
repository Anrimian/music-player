package com.github.anrimian.musicplayer.domain.models.genres

class Genre(
    val id: Long,
    val name: String,
    val compositionsCount: Int,
    val totalDuration: Long
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Genre

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Genre(id=$id, name='$name', compositionsCount=$compositionsCount, totalDuration=$totalDuration)"
    }

}