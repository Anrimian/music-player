package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable

class LibrarySettingsInteractor(
    private val settingsRepository: SettingsRepository,
    private val mediaScannerRepository: MediaScannerRepository
) {

    fun getAppConfirmDeleteDialogEnabledObservable(): Observable<Boolean> = settingsRepository.appConfirmDeleteDialogEnabledObservable

    fun setAppConfirmDeleteDialogEnabled(enabled: Boolean) {
        settingsRepository.isAppConfirmDeleteDialogEnabled = enabled
    }

    fun isAppConfirmDeleteDialogEnabled() = settingsRepository.isAppConfirmDeleteDialogEnabled

    fun geAudioFileMinDurationMillisObservable(): Observable<Long> = settingsRepository.audioFileMinDurationMillisObservable

    fun setAudioFileMinDurationMillis(millis: Long) {
        settingsRepository.audioFileMinDurationMillis = millis
        mediaScannerRepository.rescanStorage()
    }

    fun getAudioFileMinDurationMillis() = settingsRepository.audioFileMinDurationMillis

    fun setShowAllAudioFilesEnabled(enabled: Boolean) {
        settingsRepository.isShowAllAudioFilesEnabled = enabled
        mediaScannerRepository.rescanStorage()
    }

    fun getShowAllAudioFilesEnabledObservable(): Observable<Boolean> =
        settingsRepository.showAllAudioFilesEnabledObservable

}