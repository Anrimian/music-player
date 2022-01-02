package com.github.anrimian.musicplayer.ui.settings.library

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

interface LibrarySettingsView : MvpView {

    @AddToEndSingle
    fun showAppConfirmDeleteDialogEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showAllAudioFilesEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showAudioFileMinDurationMillis(millis: Long)

    @Skip
    fun showSelectMinAudioDurationDialog(currentValue: Long)

}