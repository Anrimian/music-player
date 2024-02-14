package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface LyricsView: MvpView {

    @AddToEndSingle
    fun showLyrics(text: String?)

    @Skip
    fun showEditLyricsScreen(compositionId: Long)

    @OneExecution
    fun resetTextPosition()

}