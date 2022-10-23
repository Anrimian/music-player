package com.github.anrimian.musicplayer.data.controllers.music.players

import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface AppMediaPlayer {

    fun prepareToPlay(
        source: CompositionContentSource,
        previousException: Exception? = null
    ): Completable

    fun stop()

    fun resume()

    fun pause()

    fun release()

    fun seekTo(position: Long)

    fun setVolume(volume: Float)

    fun setSoundBalance(soundBalance: SoundBalance)

    fun getTrackPosition(): Single<Long>

    fun getDuration(): Single<Long>

    fun setPlaybackSpeed(speed: Float)

    fun getTrackPositionObservable(): Observable<Long>

    fun getPlayerEventsObservable(): Observable<MediaPlayerEvent>

    fun getSpeedChangeAvailableObservable(): Observable<Boolean>

}