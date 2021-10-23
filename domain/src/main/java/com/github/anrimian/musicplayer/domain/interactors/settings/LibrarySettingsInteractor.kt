package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable

class LibrarySettingsInteractor(private val settingsRepository: SettingsRepository) {

    fun getAppConfirmDeleteDialogEnabledObservable(): Observable<Boolean> = settingsRepository.appConfirmDeleteDialogEnabledObservable

    fun setAppConfirmDeleteDialogEnabled(enabled: Boolean) {
        settingsRepository.isAppConfirmDeleteDialogEnabled = enabled
    }

    fun isAppConfirmDeleteDialogEnabled() = settingsRepository.isAppConfirmDeleteDialogEnabled

    fun geAudioFileMinDurationMillisObservable(): Observable<Long> = settingsRepository.getAudioFileMinDurationMillisObservable()

    fun setAudioFileMinDurationMillis(millis: Long) {
        settingsRepository.audioFileMinDurationMillis = millis
    }

    fun getAudioFileMinDurationMillis() = settingsRepository.audioFileMinDurationMillis

    fun setShowAllAudioFilesEnabled(enabled: Boolean) {
        settingsRepository.setShowAllAudioFilesEnabled(enabled)
    }

    fun getShowAllAudioFilesEnabledObservable(): Observable<Boolean> =
        settingsRepository.showAllAudioFilesEnabledObservable

}