package com.github.anrimian.musicplayer.domain.controllers

import io.reactivex.rxjava3.core.Observable

interface SystemServiceController {

    fun startMusicService()

    fun stopMusicService(forceStop: Boolean = false, hideUi: Boolean = false)

    fun getStopForegroundSignal(): Observable<Boolean>

}