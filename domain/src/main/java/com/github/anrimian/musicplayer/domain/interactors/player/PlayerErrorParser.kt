package com.github.anrimian.musicplayer.domain.interactors.player

interface PlayerErrorParser {

    fun parseError(throwable: Throwable): Throwable

}