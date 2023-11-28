package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository

class HeadsetSettingsInteractor(
    private val settingsRepository: SettingsRepository,
) {

    fun getBluetoothConnectAutoPlayDelay(): Long = settingsRepository.bluetoothConnectAutoPlayDelay

    fun setBluetoothConnectAutoPlayDelay(millis: Long) {
        settingsRepository.bluetoothConnectAutoPlayDelay = millis
    }
}