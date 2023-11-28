package com.github.anrimian.musicplayer.ui.settings.headset

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

interface HeadsetSettingsView: MvpView {

    @AddToEndSingle
    fun showConnectAutoPlayDelay(millis: Long)

    @Skip
    fun showPlayDelayPickerDialog(currentValue: Long)

}