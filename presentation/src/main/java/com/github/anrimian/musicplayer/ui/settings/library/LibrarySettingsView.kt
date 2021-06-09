package com.github.anrimian.musicplayer.ui.settings.library

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface LibrarySettingsView : MvpView {

    @AddToEndSingle
    fun showAppConfirmDeleteDialogEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showAudioFileMinDurationMillis(millis: Long)

}