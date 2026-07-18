package com.github.anrimian.musicplayer.wear.domain.models

class PlayQueueItem(
    val id: Long,
    val itemId: Long,
    val title: String,
    val artist: String?,
    val duration: Long
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayQueueItem) return false

        if (itemId != other.itemId) return false

        return true
    }

    override fun hashCode(): Int {
        return itemId.hashCode()
    }

}