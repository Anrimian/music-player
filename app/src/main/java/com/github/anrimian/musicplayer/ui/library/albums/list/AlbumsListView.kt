package com.github.anrimian.musicplayer.ui.library.albums.list

import com.github.anrimian.musicplayer.domain.models.albums.Album
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

interface AlbumsListView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptySearchResult()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoadingError(errorCommand: ErrorCommand?)

    @AddToEndSingle
    fun submitList(albums: List<Album>)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun showSelectOrderScreen(order: Order)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

    @Skip
    fun goToAlbumScreen(album: Album)

    @Skip
    fun onAlbumSelected(album: Album, position: Int)

    @Skip
    fun onAlbumUnselected(album: Album, position: Int)

    @Skip
    fun setItemsSelected(selected: Boolean)

    @AddToEndSingle
    fun showSelectionMode(count: Int)

    @OneExecution
    fun onCompositionsAddedToPlayNext(compositions: List<Composition>)

    @OneExecution
    fun onCompositionsAddedToQueue(compositions: List<Composition>)

    @OneExecution
    fun showSelectPlayListDialog(albums: Collection<Album>, closeMultiselect: Boolean)

    @OneExecution
    fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>)

    @OneExecution
    fun showAddingToPlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun sendCompositions(compositions: List<Composition>)

    @OneExecution
    fun showReceiveCompositionsForSendError(errorCommand: ErrorCommand)
}