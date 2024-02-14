package com.github.anrimian.musicplayer.ui.playlist_screens.rename

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.OneExecution

private const val CHANGE_STATE = "change_state"

interface RenamePlayListView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun showProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun showInputState()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = CHANGE_STATE)
    fun showError(errorCommand: ErrorCommand)

    @OneExecution
    fun closeScreen()

    @OneExecution
    fun showPlayListName(initialName: String?)

}