package com.github.anrimian.musicplayer.ui.main.external_player

import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface ExternalPlayerView : MvpView {

    @AddToEndSingle
    fun showPlayerState(isPlaying: Boolean)

    @AddToEndSingle
    fun displayComposition(source: ExternalCompositionSource)

    @AddToEndSingle
    fun showTrackState(currentPosition: Long, duration: Long)

    @AddToEndSingle
    fun showRepeatMode(mode: Int)

    @AddToEndSingle
    fun showPlayErrorState(errorCommand: ErrorCommand?)

    @AddToEndSingle
    fun showKeepPlayerInBackground(externalPlayerKeepInBackground: Boolean)

    @AddToEndSingle
    fun displayPlaybackSpeed(speed: Float)

    @AddToEndSingle
    fun showSpeedChangeFeatureVisible(visible: Boolean)

}