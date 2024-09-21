package com.github.anrimian.musicplayer.domain.models.player

sealed interface PlayerState {
    data object IDLE: PlayerState
    data object LOADING: PlayerState
    data object PREPARING: PlayerState
    data object PLAY: PlayerState
    data object PAUSE: PlayerState
    data object STOP: PlayerState
    data class Error(val throwable: Throwable): PlayerState
}