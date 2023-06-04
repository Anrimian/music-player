package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable

class DisplaySettingsInteractor(private val settingsRepository: SettingsRepository) {

    fun isDisplayFileNameEnabled() = settingsRepository.isDisplayFileNameEnabled

    fun setDisplayFileName(useFileName: Boolean) {
        settingsRepository.setDisplayFileName(useFileName)
    }

    fun getCoversEnabledObservable(): Observable<Boolean> = settingsRepository.coversEnabledObservable

    fun getCoversInNotificationEnabledObservable(): Observable<Boolean> = settingsRepository.coversInNotificationEnabledObservable

    fun isCoversEnabled() = settingsRepository.isCoversEnabled

    fun setCoversEnabled(enabled: Boolean) {
        settingsRepository.isCoversEnabled = enabled
    }

    fun isCoversInNotificationEnabled() = settingsRepository.isCoversInNotificationEnabled

    fun setCoversInNotificationEnabled(enabled: Boolean) {
        settingsRepository.isCoversInNotificationEnabled = enabled
    }

    fun isColoredNotificationEnabled() = settingsRepository.isColoredNotificationEnabled

    fun setColoredNotificationEnabled(enabled: Boolean) {
        settingsRepository.isColoredNotificationEnabled = enabled
    }

    fun isCoversOnLockScreenEnabled() = settingsRepository.isCoversOnLockScreenEnabled

    fun setCoversOnLockScreenEnabled(enabled: Boolean) {
        settingsRepository.isCoversOnLockScreenEnabled = enabled
    }

    fun isNotificationCoverStubEnabled() = settingsRepository.isNotificationCoverStubEnabled

    fun setNotificationCoverStubEnabled(enabled: Boolean) {
        settingsRepository.isNotificationCoverStubEnabled = enabled
    }

    fun isPlayerScreensSwipeEnabled() = settingsRepository.isPlayerScreensSwipeEnabled

    fun setPlayerScreensSwipeEnabled(enabled: Boolean) {
        settingsRepository.isPlayerScreensSwipeEnabled = enabled
    }

}