package com.github.anrimian.musicplayer.ui.settings.player.impls

import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor
import moxy.MvpPresenter

class EnabledMediaPlayersPresenter(
    private val playerSettingsInteractor: PlayerSettingsInteractor
): MvpPresenter<EnabledMediaPlayersView>() {

    private lateinit var mediaPlayers: IntArray

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        mediaPlayers = playerSettingsInteractor.getEnabledMediaPlayers()
        viewState.showEnabledMediaPlayers(mediaPlayers)
    }

    fun onCompleteButtonClicked() {
        viewState.close(mediaPlayers)
    }
}