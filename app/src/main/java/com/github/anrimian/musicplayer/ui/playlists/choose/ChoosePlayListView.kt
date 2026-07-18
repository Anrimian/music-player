package com.github.anrimian.musicplayer.ui.playlists.choose

import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

private const val LIST_STATE = "list_state"

interface ChoosePlayListView : MvpView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showErrorState(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun updateList(list: List<Playlist>)

    @AddToEndSingle
    fun showBottomSheetSlided(slideOffset: Float)

    @OneExecution
    fun showConfirmDeletePlayListDialog(playList: Playlist)

    @OneExecution
    fun showPlayListDeleteSuccess(playList: Playlist)

    @OneExecution
    fun showDeletePlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun showEditPlayListNameDialog(playList: Playlist)

}