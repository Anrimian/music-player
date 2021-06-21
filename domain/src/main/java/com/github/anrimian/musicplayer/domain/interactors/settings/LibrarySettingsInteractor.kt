package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository

class LibrarySettingsInteractor(private val settingsRepository: SettingsRepository) {

    fun getAppConfirmDeleteDialogEnabledObservable() = settingsRepository.appConfirmDeleteDialogEnabledObservable

    fun setAppConfirmDeleteDialogEnabled(enabled: Boolean) {
        settingsRepository.isAppConfirmDeleteDialogEnabled = enabled
    }

    fun isAppConfirmDeleteDialogEnabled() = settingsRepository.isAppConfirmDeleteDialogEnabled

    fun geAudioFileMinDurationMillisObservable() = settingsRepository.geAudioFileMinDurationMillisObservable()

    fun setAudioFileMinDurationMillis(millis: Long) {
        settingsRepository.audioFileMinDurationMillis = millis
    }

    fun getAudioFileMinDurationMillis() = settingsRepository.audioFileMinDurationMillis

}