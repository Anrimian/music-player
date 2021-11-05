package com.github.anrimian.musicplayer.ui.settings.player

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface PlayerSettingsView : MvpView {

    @AddToEndSingle
    fun showDecreaseVolumeOnAudioFocusLossEnabled(checked: Boolean)

    @AddToEndSingle
    fun showPauseOnAudioFocusLossEnabled(checked: Boolean)

    @AddToEndSingle
    fun showPauseOnZeroVolumeLevelEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showSelectedEqualizerType(type: Int)

    @AddToEndSingle
    fun showEnabledMediaPlayers(players: IntArray)

}