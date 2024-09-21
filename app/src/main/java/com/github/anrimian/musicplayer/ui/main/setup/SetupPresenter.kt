package com.github.anrimian.musicplayer.ui.main.setup

import moxy.MvpPresenter

/**
 * Created on 19.10.2017.
 */
class SetupPresenter : MvpPresenter<SetupView>() {

    private var isPermissionGranted = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        requestFilesPermissions()
    }

    fun onFilesPermissionResult(granted: Boolean) {
        if (granted && !isPermissionGranted) {
            isPermissionGranted = true
            viewState.startSystemServices()
            viewState.goToMainScreen()
        } else {
            viewState.showDeniedPermissionMessage()
        }
    }

    fun onTryAgainButtonClicked() {
        requestFilesPermissions()
    }

    private fun requestFilesPermissions() {
        viewState.showStub()
        viewState.requestFilesPermissions()
    }
}