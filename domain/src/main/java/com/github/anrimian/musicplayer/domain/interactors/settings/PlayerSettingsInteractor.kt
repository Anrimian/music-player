package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable

class PlayerSettingsInteractor(private val settingsRepository: SettingsRepository) {

    fun isDecreaseVolumeOnAudioFocusLossEnabled() = settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled

    fun setDecreaseVolumeOnAudioFocusLossEnabled(enabled: Boolean) {
        settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled = enabled
    }

    fun isPauseOnAudioFocusLossEnabled() = settingsRepository.isPauseOnAudioFocusLossEnabled

    fun setPauseOnAudioFocusLossEnabled(enabled: Boolean) {
        settingsRepository.isPauseOnAudioFocusLossEnabled = enabled
    }

    fun isPauseOnZeroVolumeLevelEnabled() = settingsRepository.isPauseOnZeroVolumeLevelEnabled

    fun setPauseOnZeroVolumeLevelEnabled(enabled: Boolean) {
        settingsRepository.isPauseOnZeroVolumeLevelEnabled = enabled
    }

    fun getSelectedEqualizerTypeObservable(): Observable<Int> = settingsRepository.selectedEqualizerTypeObservable

    fun getEnabledMediaPlayers(): IntArray = settingsRepository.enabledMediaPlayers

    fun setEnabledMediaPlayers(players: IntArray) {
        if (players.isEmpty() || players.contentEquals(settingsRepository.enabledMediaPlayers)) {
            return
        }
        settingsRepository.enabledMediaPlayers = players
    }
}