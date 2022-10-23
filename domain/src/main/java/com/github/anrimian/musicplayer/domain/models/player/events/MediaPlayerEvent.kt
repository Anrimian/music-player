package com.github.anrimian.musicplayer.domain.models.player.events

sealed interface MediaPlayerEvent {
    object Finished: MediaPlayerEvent
    class Error(val throwable: Throwable): MediaPlayerEvent
}