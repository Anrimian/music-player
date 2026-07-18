package com.github.anrimian.musicplayer.ui.settings.headset

import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.MessageAction
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

data class HeadsetSettingsState(
    val bluetoothConnectAutoPlayDelay: Long = 0,
    val isProcessUnsupportedBluetoothEventEnabled: Boolean = false,
    val isIgnorePlayAfterConnectionEnabled: Boolean = false,
    val isBluetoothAutoPlayEnabled: Boolean = false,
)

sealed interface HeadsetSettingsDialogs : AppDialog {
    @Parcelize
    data class PlayDelayPickerDialog(val currentValue: Long) : HeadsetSettingsDialogs
}

sealed interface HeadsetSettingsEffect : BaseEffect {
    data class SetBluetoothReceiverEnabled(val isEnabled: Boolean) : HeadsetSettingsEffect
    object RequestBluetoothConnectPermission : HeadsetSettingsEffect
}

data object OpenAppSettings : MessageAction
