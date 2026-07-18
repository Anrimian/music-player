package com.github.anrimian.musicplayer.wear.ui

import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.wear.domain.models.DeviceState
import com.github.anrimian.musicplayer.wear.domain.models.ErrorEvent
import com.github.anrimian.musicplayer.wear.domain.models.PlayQueueItem
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface MainView: MvpView {

    @AddToEndSingle
    fun showIsPlaying(isPlaying: Boolean)

    @AddToEndSingle
    fun showComposition(compositionOpt: Opt<WearableComposition>)

    @AddToEndSingle
    fun showDeviceState(deviceState: DeviceState)

    @AddToEndSingle
    fun showTrackState(trackPosition: Long, duration: Long)

    @OneExecution
    fun showErrorEvent(errorEvent: ErrorEvent)

    @AddToEndSingle
    fun showCurrentVolume(volume: Long)

    @AddToEndSingle
    fun updatePlayQueue(playQueueItems: List<PlayQueueItem>)

}