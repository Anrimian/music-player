package com.github.anrimian.musicplayer.ui.settings.headset

import com.github.anrimian.musicplayer.domain.interactors.settings.HeadsetSettingsInteractor
import moxy.MvpPresenter

class HeadsetSettingsPresenter(
    private val interactor: HeadsetSettingsInteractor
): MvpPresenter<HeadsetSettingsView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showConnectAutoPlayDelay(interactor.getBluetoothConnectAutoPlayDelay())
    }

    fun onPickPlayDelayClicked() {
        viewState.showPlayDelayPickerDialog(interactor.getBluetoothConnectAutoPlayDelay())
    }

    fun onConnectAutoPlayDelaySelected(millis: Long) {
        interactor.setBluetoothConnectAutoPlayDelay(millis)
        viewState.showConnectAutoPlayDelay(millis)
    }

}