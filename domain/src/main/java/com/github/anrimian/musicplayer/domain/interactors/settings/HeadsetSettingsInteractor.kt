package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository

class HeadsetSettingsInteractor(
    private val settingsRepository: SettingsRepository,
) {

    fun getBluetoothConnectAutoPlayDelay(): Long = settingsRepository.bluetoothConnectAutoPlayDelay

    fun setBluetoothConnectAutoPlayDelay(millis: Long) {
        settingsRepository.bluetoothConnectAutoPlayDelay = millis
    }

    fun isProcessUnsupportedBluetoothEventEnabled(): Boolean {
        return settingsRepository.isProcessUnsupportedBluetoothEventEnabled
    }

    fun setProcessUnsupportedBluetoothEventEnabled(enabled: Boolean) {
        settingsRepository.isProcessUnsupportedBluetoothEventEnabled = enabled
    }

    fun isIgnorePlayAfterConnectionEnabled(): Boolean {
        return settingsRepository.isIgnorePlayAfterConnectionEnabled
    }

    fun setIgnorePlayAfterConnectionEnabled(enabled: Boolean) {
        settingsRepository.isIgnorePlayAfterConnectionEnabled = enabled
    }

    fun isBluetoothAutoPlayEnabled() = settingsRepository.isBluetoothAutoPlayEnabled

    fun setBluetoothAutoPlayEnabled(enabled: Boolean) {
        settingsRepository.isBluetoothAutoPlayEnabled = enabled
    }

}