package com.github.anrimian.musicplayer.ui.settings.folders

import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

private const val LIST_STATE = "list_state"

interface ExcludedFoldersView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showListState()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyListState()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showErrorState(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun showExcludedFoldersList(folders: List<IgnoredFolder>)

    @OneExecution
    fun showRemovedFolderMessage(folder: IgnoredFolder)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

}