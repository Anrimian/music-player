package com.github.anrimian.musicplayer.domain.models.play_queue

/**
 * Created on 01.05.2018.
 */
//remake to optional
class PlayQueueEvent(val playQueueItem: PlayQueueItem?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayQueueEvent

        return playQueueItem == other.playQueueItem
    }

    override fun hashCode(): Int {
        return playQueueItem?.hashCode() ?: 0
    }
}