package com.github.anrimian.musicplayer.ui.settings.headset

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.settings.HeadsetSettingsInteractor
import com.github.anrimian.musicplayer.ui.common.effects.MessageDuration
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.BaseViewModel
import com.github.anrimian.musicplayer.ui.common.mvvm.EmptyPersistent
import com.github.anrimian.musicplayer.ui.utils.compose.UiText

class HeadsetSettingsViewModel(
    private val interactor: HeadsetSettingsInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
): BaseViewModel<HeadsetSettingsState, EmptyPersistent>(
    HeadsetSettingsState(),
    EmptyPersistent,
    savedStateHandle,
    errorParser
) {

    init {
        updateState {
            copy(
                bluetoothConnectAutoPlayDelay = interactor.getBluetoothConnectAutoPlayDelay(),
                isProcessUnsupportedBluetoothEventEnabled = interactor.isProcessUnsupportedBluetoothEventEnabled(),
                isIgnorePlayAfterConnectionEnabled = interactor.isIgnorePlayAfterConnectionEnabled(),
                isBluetoothAutoPlayEnabled = interactor.isBluetoothAutoPlayEnabled()
            )
        }
    }

    private var isPlayOnConnectRequest = true

    fun onPlayOnConnectChecked(isChecked: Boolean, hasPermission: Boolean) {
        if (isChecked && !hasPermission) {
            isPlayOnConnectRequest = true
            sendEffect(HeadsetSettingsEffect.RequestBluetoothConnectPermission)
            return
        }
        interactor.setBluetoothAutoPlayEnabled(isChecked)
        updateState { copy(isBluetoothAutoPlayEnabled = isChecked) }
        updateReceiverEnabledState()
    }

    fun onConnectAutoPlayDelaySelected(millis: Long) {
        interactor.setBluetoothConnectAutoPlayDelay(millis)
        updateState { copy(bluetoothConnectAutoPlayDelay = millis) }
        dismissDialog()
    }

    fun onConnectAutoPlayDelayDismissed() {
        dismissDialog()
    }

    fun onProcessUnsupportedEventsChecked(isChecked: Boolean) {
        interactor.setProcessUnsupportedBluetoothEventEnabled(isChecked)
        updateState { copy(isProcessUnsupportedBluetoothEventEnabled = isChecked) }
    }

    fun onIgnorePlayAfterConnectionChecked(isChecked: Boolean, hasPermission: Boolean) {
        if (isChecked && !hasPermission) {
            isPlayOnConnectRequest = false
            sendEffect(HeadsetSettingsEffect.RequestBluetoothConnectPermission)
            return
        }
        interactor.setIgnorePlayAfterConnectionEnabled(isChecked)
        updateState { copy(isIgnorePlayAfterConnectionEnabled = isChecked) }
        updateReceiverEnabledState()
    }

    fun onBluetoothConnectPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        if (granted) {
            if (isPlayOnConnectRequest) {
                onPlayOnConnectChecked(isChecked = true, hasPermission = true)
            } else {
                onIgnorePlayAfterConnectionChecked(isChecked = true, hasPermission = true)
            }
        } else {
            if (!shouldShowRationale) {
                sendMessage(
                    message = UiText.StringResource(R.string.permission_required),
                    actionLabel = UiText.StringResource(R.string.open_app_settings),
                    action = OpenAppSettings,
                    duration = MessageDuration.Indefinite
                )
            }
        }
    }

    fun onBluetoothConnectPermissionRevoked() {
        if (currentState.isBluetoothAutoPlayEnabled || currentState.isIgnorePlayAfterConnectionEnabled) {
            interactor.setBluetoothAutoPlayEnabled(false)
            interactor.setIgnorePlayAfterConnectionEnabled(false)
            updateState {
                copy(
                    isBluetoothAutoPlayEnabled = false,
                    isIgnorePlayAfterConnectionEnabled = false
                )
            }
            updateReceiverEnabledState()
        }
    }

    fun onPickPlayDelayClicked() {
        showDialog(HeadsetSettingsDialogs.PlayDelayPickerDialog(currentState.bluetoothConnectAutoPlayDelay))
    }

    private fun updateReceiverEnabledState() {
        val isEnabled = currentState.isBluetoothAutoPlayEnabled || currentState.isIgnorePlayAfterConnectionEnabled
        sendEffect(HeadsetSettingsEffect.SetBluetoothReceiverEnabled(isEnabled))
    }
}
