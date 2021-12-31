package com.github.anrimian.musicplayer.ui.settings.player.impls

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

interface EnabledMediaPlayersView: MvpView {

    @AddToEndSingle
    fun showMediaPlayers(mediaPlayers: List<Int>)

    @AddToEndSingle
    fun showEnabledMediaPlayers(mediaPlayers: Set<Int>)

    @AddToEndSingle
    fun setDisableAllowed(allowed: Boolean)

    @Skip
    fun close(result: IntArray)

    @Skip
    fun notifyItemMoved(from: Int, to: Int)

}