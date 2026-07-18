package com.github.anrimian.musicplayer.data.storage.providers.volumes

import android.content.Context
import com.github.anrimian.musicplayer.data.storage.providers.FileVolume
import com.github.anrimian.musicplayer.data.storage.providers.MediaStoreUtils

class VolumeProviderImpl(private val context: Context) : VolumeProvider {

    override fun getVolumes(): Map<String, FileVolume> {
        return MediaStoreUtils.getVolumes(context)
    }

}
