package com.github.anrimian.musicplayer.ui.playlist_screens.playlist

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy
import moxy.MvpView
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val LIST_STATE = "list_state"

interface PlayListView : MvpView {
    
    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @AddToEndSingle
    fun updateItemsList(list: List<PlayListItem>)

    @OneExecution
    fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>)

    @OneExecution
    fun closeScreen()

    @AddToEndSingle
    fun showPlayListInfo(playList: PlayList)

    @OneExecution
    fun showDeleteCompositionError(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeletedCompositionMessage(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showSelectPlayListDialog()

    @OneExecution
    fun showAddingToPlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>)

    @OneExecution
    fun showDeleteItemError(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeleteItemCompleted(playList: PlayList, items: List<PlayListItem>)

    @OneExecution
    fun showConfirmDeletePlayListDialog(playList: PlayList)

    @OneExecution
    fun showPlayListDeleteSuccess(playList: PlayList)

    @OneExecution
    fun showDeletePlayListError(errorCommand: ErrorCommand)

    @Skip
    fun notifyItemMoved(from: Int, to: Int)

    @Skip
    fun showCompositionActionDialog(playListItem: PlayListItem, position: Int)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun notifyItemRemoved(position: Int)

    @OneExecution
    fun showEditPlayListNameDialog(playList: PlayList)

    @OneExecution
    fun onCompositionsAddedToPlayNext(compositions: List<Composition>)

    @OneExecution
    fun onCompositionsAddedToQueue(compositions: List<Composition>)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

}