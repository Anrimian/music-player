package com.github.anrimian.musicplayer.ui.start

import moxy.MvpPresenter

/**
 * Created on 19.10.2017.
 */
class StartPresenter : MvpPresenter<StartView>() {
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        requestFilesPermissions()
    }

    fun onFilesPermissionResult(granted: Boolean) {
        if (granted) {
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