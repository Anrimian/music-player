package com.github.anrimian.musicplayer.data.controllers.music.equalizer

import android.app.Activity
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.external.ExternalEqualizer
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository

class EqualizerController(
    private val settingsRepository: SettingsRepository,
    private val externalEqualizer: ExternalEqualizer,
    private val internalEqualizer: InternalEqualizer
) {

    private var audioSessionId = 0
    private var currentEqualizer: AppEqualizer? = null

    fun attachEqualizer(audioSessionId: Int) {
        this.audioSessionId = audioSessionId
        val type = settingsRepository.selectedEqualizerType
        if (type == EqualizerType.NONE) {
            return
        }
        currentEqualizer = selectEqualizerByType(type).apply {
            attachEqualizer(audioSessionId)
        }
    }

    fun detachEqualizer() {
        currentEqualizer?.detachEqualizer(audioSessionId)
    }

    fun disableEqualizer() {
        currentEqualizer?.detachEqualizer(audioSessionId)
        settingsRepository.selectedEqualizerType = EqualizerType.NONE
    }

    fun enableEqualizer(type: Int) {
        settingsRepository.selectedEqualizerType = type
        currentEqualizer = selectEqualizerByType(type).apply {
            attachEqualizer(audioSessionId)
        }
    }

    fun launchExternalEqualizerSetup(activity: Activity) {
        settingsRepository.selectedEqualizerType = EqualizerType.EXTERNAL
        currentEqualizer = externalEqualizer
        externalEqualizer.launchExternalEqualizerSetup(activity, audioSessionId)
    }

    fun getSelectedEqualizerType() = settingsRepository.selectedEqualizerType

    private fun selectEqualizerByType(type: Int): AppEqualizer {
        return when (type) {
            EqualizerType.EXTERNAL -> externalEqualizer
            EqualizerType.APP -> internalEqualizer
            else -> throw IllegalStateException("unknown equalizer type: $type")
        }
    }
}