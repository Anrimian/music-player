package com.github.anrimian.musicplayer.ui.settings.player.impls

import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.indexOfOr
import moxy.MvpPresenter
import java.util.*
import kotlin.collections.HashSet

class EnabledMediaPlayersPresenter(
    private val playerSettingsInteractor: PlayerSettingsInteractor
): MvpPresenter<EnabledMediaPlayersView>() {

    private val defaultMediaPlayers = intArrayOf(
        MediaPlayers.EXO_MEDIA_PLAYER,
        MediaPlayers.ANDROID_MEDIA_PLAYER
    )

    private lateinit var mediaPlayers: List<Int>

    private val enabledMediaPlayers = HashSet<Int>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        val enabledMediaPlayers = playerSettingsInteractor.getEnabledMediaPlayers()
        enabledMediaPlayers.forEach(this.enabledMediaPlayers::add)

        mediaPlayers = defaultMediaPlayers.sortedBy { id -> enabledMediaPlayers.indexOfOr(id, defaultMediaPlayers.size) }
        viewState.showMediaPlayers(mediaPlayers)

        viewState.showEnabledMediaPlayers(this.enabledMediaPlayers)
        showAllowedItemDisabling()
    }

    fun onItemMoved(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                swapItems(i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                swapItems(i, i - 1)
            }
        }
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

    fun onResetButtonClicked() {
        viewState.close(defaultMediaPlayers)
    }

    private fun swapItems(from: Int, to: Int) {
        if (!ListUtils.isIndexInRange(mediaPlayers, from) || !ListUtils.isIndexInRange(mediaPlayers, to)) {
            return
        }

        Collections.swap(mediaPlayers, from, to)
        viewState.notifyItemMoved(from, to)
    }

    private fun showAllowedItemDisabling() {
        viewState.setDisableAllowed(enabledMediaPlayers.size > 1)
    }


}