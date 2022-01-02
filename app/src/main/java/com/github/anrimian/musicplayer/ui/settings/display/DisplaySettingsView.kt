package com.github.anrimian.musicplayer.ui.settings.display

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface DisplaySettingsView : MvpView {

    @AddToEndSingle
    fun showFileNameEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showCoversChecked(checked: Boolean)

    @AddToEndSingle
    fun showCoversInNotificationChecked(checked: Boolean)

    @AddToEndSingle
    fun showColoredNotificationChecked(checked: Boolean)

    @AddToEndSingle
    fun showNotificationCoverStubChecked(checked: Boolean)

    @AddToEndSingle
    fun showCoversOnLockScreenChecked(checked: Boolean)

    @AddToEndSingle
    fun showCoversInNotificationEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showColoredNotificationEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showNotificationCoverStubEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showCoversOnLockScreenEnabled(enabled: Boolean)

}