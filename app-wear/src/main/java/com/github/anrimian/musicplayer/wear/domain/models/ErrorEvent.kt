package com.github.anrimian.musicplayer.wear.domain.models

data class ErrorEvent(
    val errorType: Int,
    val eventName: String,
    val throwable: Throwable? = null
)