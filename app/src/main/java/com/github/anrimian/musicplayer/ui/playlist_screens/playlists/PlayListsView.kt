package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

private const val LIST_STATE = "list_state"

interface PlayListsView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @AddToEndSingle
    fun updateList(lists: List<PlayList>)

    @OneExecution
    fun showConfirmDeletePlayListDialog(playList: PlayList)

    @OneExecution
    fun showPlayListDeleteSuccess(playList: PlayList)

    @OneExecution
    fun showDeletePlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun showEditPlayListNameDialog(playList: PlayList)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

}