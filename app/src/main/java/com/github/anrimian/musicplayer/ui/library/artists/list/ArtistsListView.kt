package com.github.anrimian.musicplayer.ui.library.artists.list

import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

const val LIST_STATE = "list_state"
const val RENAME_STATE = "rename_state"

interface ArtistsListView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptySearchResult()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoadingError(errorCommand: ErrorCommand)

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = RENAME_STATE)
    fun showRenameProgress()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = RENAME_STATE)
    fun hideRenameProgress()

    @AddToEndSingle
    fun submitList(artists: List<Artist>)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun showSelectOrderScreen(order: Order)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

}