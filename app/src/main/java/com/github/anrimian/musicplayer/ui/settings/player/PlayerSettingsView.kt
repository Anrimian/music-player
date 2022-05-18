package com.github.anrimian.musicplayer.ui.settings.player

import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

interface PlayerSettingsView : MvpView {

    @AddToEndSingle
    fun showDecreaseVolumeOnAudioFocusLossEnabled(checked: Boolean)

    @AddToEndSingle
    fun showPauseOnAudioFocusLossEnabled(checked: Boolean)

    @AddToEndSingle
    fun showPauseOnZeroVolumeLevelEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showSoundBalance(soundBalance: SoundBalance)

    @AddToEndSingle
    fun showSelectedEqualizerType(type: Int)

    @AddToEndSingle
    fun showEnabledMediaPlayers(players: IntArray)

    @Skip
    fun showSoundBalanceDialog(soundBalance: SoundBalance)

}