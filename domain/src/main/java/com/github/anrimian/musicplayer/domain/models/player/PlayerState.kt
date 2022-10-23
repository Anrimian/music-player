package com.github.anrimian.musicplayer.domain.models.player

sealed interface PlayerState {
    object IDLE: PlayerState
    object LOADING: PlayerState
    object PREPARING: PlayerState
    object PLAY: PlayerState
    object PAUSE: PlayerState
    object STOP: PlayerState
    data class Error(val throwable: Throwable): PlayerState
}