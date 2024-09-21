package com.github.anrimian.musicplayer.ui.playlist_screens.playlist

import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface PlayListView : BaseLibraryView {
    
    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptySearchResult()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
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
    fun showDeletedCompositionMessage(compositionsToDelete: List<DeletedComposition>)

    @OneExecution
    fun showSelectPlayListDialog()

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

    @OneExecution
    fun notifyItemRemoved(position: Int)

    @OneExecution
    fun showEditPlayListNameDialog(playList: PlayList)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

    @AddToEndSingle
    fun showRandomMode(isRandomModeEnabled: Boolean)

    @AddToEndSingle
    fun showCurrentComposition(currentComposition: CurrentComposition)

    @AddToEndSingle
    fun setDragEnabled(enabled: Boolean)

    @AddToEndSingle
    fun showFilesSyncState(states: Map<Long, FileSyncState>)

    @OneExecution
    fun showPlaylistExportSuccess(playlist: PlayList)

    companion object {
        private const val LIST_STATE = "list_state"
    }
}