package com.github.anrimian.musicplayer.ui.library.folders.root

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.OneExecution

private const val LOADING_STATE = "loading_state"

interface FolderRootView : MvpView {

    @OneExecution
    fun showFolderScreens(ids: List<Long?>)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LOADING_STATE)
    fun showProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LOADING_STATE)
    fun showError(errorCommand: ErrorCommand)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LOADING_STATE)
    fun showIdle()

}