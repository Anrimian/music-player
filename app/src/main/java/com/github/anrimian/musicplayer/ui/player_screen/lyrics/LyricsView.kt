package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val CHANGE_FILE_STATE = "change_file_state"

interface LyricsView: MvpView {

    @AddToEndSingle
    fun showLyrics(text: String?)

    @Skip
    fun showEnterLyricsDialog(lyrics: String)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_FILE_STATE)
    fun showChangeFileProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_FILE_STATE)
    fun hideChangeFileProgress()

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun resetTextPosition()
}