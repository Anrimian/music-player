package com.github.anrimian.musicplayer.data.controllers.music.equalizer

interface AppEqualizer {

    fun attachEqualizer(audioSessionId: Int)

    fun detachEqualizer(audioSessionId: Int)

}