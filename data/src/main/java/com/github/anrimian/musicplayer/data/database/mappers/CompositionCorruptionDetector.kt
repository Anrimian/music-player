package com.github.anrimian.musicplayer.data.database.mappers

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageAudioFile
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType

object CompositionCorruptionDetector {

    fun getCorruptionType(composition: StorageAudioFile): CorruptionType? {
        return getCorruptionType(composition.duration)
    }

    fun getCorruptionType(duration: Long): CorruptionType? {
        return if (duration == 0L) {
            CorruptionType.UNKNOWN
        } else {
            null
        }
    }

}
