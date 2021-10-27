package com.github.anrimian.musicplayer.ui.settings.player.impls

import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers
import moxy.MvpPresenter

class EnabledMediaPlayersPresenter(
    private val playerSettingsInteractor: PlayerSettingsInteractor
): MvpPresenter<EnabledMediaPlayersView>() {

    private val mediaPlayers = intArrayOf(
        MediaPlayers.EXO_MEDIA_PLAYER,
        MediaPlayers.ANDROID_MEDIA_PLAYER
    )
    private val enabledMediaPlayers = HashSet<Int>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        val enabledMediaPlayers = playerSettingsInteractor.getEnabledMediaPlayers()
        enabledMediaPlayers.forEach(this.enabledMediaPlayers::add)

        viewState.showMediaPlayers(mediaPlayers)

        viewState.showEnabledMediaPlayers(this.enabledMediaPlayers)
    }

    fun onItemMoved() {

    }

    fun onItemEnableStatusChanged(id: Int, enabled: Boolean) {
        if (enabled) {
            enabledMediaPlayers.add(id)
        } else {
            enabledMediaPlayers.remove(id)
        }
    }

    fun onCompleteButtonClicked() {
        viewState.close(mediaPlayers)
    }
}