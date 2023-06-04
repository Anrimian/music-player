package com.github.anrimian.musicplayer.ui.library.artists.list

import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val LIST_STATE = "list_state"

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

    @AddToEndSingle
    fun submitList(artists: List<Artist>)

    @OneExecution
    fun showSelectOrderScreen(order: Order)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

    @Skip
    fun goToArtistScreen(artist: Artist)

    @Skip
    fun onArtistSelected(artist: Artist, position: Int)

    @Skip
    fun onArtistUnselected(artist: Artist, position: Int)

    @Skip
    fun setItemsSelected(selected: Boolean)

    @AddToEndSingle
    fun showSelectionMode(count: Int)

    @OneExecution
    fun onCompositionsAddedToPlayNext(compositions: List<Composition>)

    @OneExecution
    fun onCompositionsAddedToQueue(compositions: List<Composition>)

    @OneExecution
    fun showSelectPlayListDialog(artists: Collection<Artist>, closeMultiselect: Boolean)

    @OneExecution
    fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>)

    @OneExecution
    fun showAddingToPlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun sendCompositions(compositions: List<Composition>)

    @OneExecution
    fun showReceiveCompositionsForSendError(errorCommand: ErrorCommand)
}