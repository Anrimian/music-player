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
        showAllowedItemDisabling()
    }

    fun onItemMoved(from: Int, to: Int) {

    }

    fun onItemEnableStatusChanged(id: Int, enabled: Boolean) {
        if (enabled) {
            enabledMediaPlayers.add(id)
        } else {
            enabledMediaPlayers.remove(id)
        }
        showAllowedItemDisabling()
    }

    fun onCompleteButtonClicked() {
        val result = IntArray(enabledMediaPlayers.size)
        var index = 0
        mediaPlayers.forEach { id ->
            if (enabledMediaPlayers.contains(id)) {
                result[index++] = id
            }
        }
        viewState.close(result)
    }

    private fun showAllowedItemDisabling() {
        viewState.setDisableAllowed(enabledMediaPlayers.size > 1)
    }
}