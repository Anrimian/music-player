package com.github.anrimian.musicplayer.data.storage.providers.volumes

import com.github.anrimian.musicplayer.data.storage.providers.FileVolume

interface VolumeProvider {

    fun getVolumes(): Map<String, FileVolume>

}
