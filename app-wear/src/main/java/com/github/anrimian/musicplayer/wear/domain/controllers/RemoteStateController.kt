package com.github.anrimian.musicplayer.wear.domain.controllers

import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState

interface RemoteStateController {

    fun setIsPlaying(isPlaying: Boolean)

    fun setVolumeState(volumeState: VolumeState)

    fun setCurrentComposition(composition: WearableComposition?)

}