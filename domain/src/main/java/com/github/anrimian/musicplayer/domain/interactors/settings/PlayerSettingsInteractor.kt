package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable

class PlayerSettingsInteractor(
    private val settingsRepository: SettingsRepository,
    private val mediaPlayerController: MusicPlayerController
) {

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

    fun getSoundBalance(): SoundBalance = settingsRepository.soundBalance

    fun setSoundBalance(soundBalance: SoundBalance) {
        mediaPlayerController.setSoundBalance(soundBalance)
    }

    fun saveSoundBalance(soundBalance: SoundBalance) {
        mediaPlayerController.setSoundBalance(soundBalance)
        settingsRepository.soundBalance = soundBalance
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