package com.github.anrimian.musicplayer.domain.models.playlist

import com.github.anrimian.musicplayer.domain.models.composition.Composition

class PlayListItem(
    val itemId: Long,
    val composition: Composition
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayListItem

        if (itemId != other.itemId) return false

        return true
    }

    override fun hashCode(): Int {
        return itemId.hashCode()
    }

    override fun toString(): String {
        return "PlayListItem(itemId=$itemId, composition=$composition)"
    }

}