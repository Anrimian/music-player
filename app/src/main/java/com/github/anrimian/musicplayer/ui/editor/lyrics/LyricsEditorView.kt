package com.github.anrimian.musicplayer.ui.editor.lyrics

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.OneExecution

interface LyricsEditorView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = STATE)
    fun showLyrics(text: String)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = STATE)
    fun showLyricsLoadingError(errorCommand: ErrorCommand)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_FILE_STATE)
    fun showChangeFileProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_FILE_STATE)
    fun hideChangeFileProgress()

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun closeScreen()

    private companion object {
        const val STATE = "state"
        const val CHANGE_FILE_STATE = "change_file_state"
    }

}