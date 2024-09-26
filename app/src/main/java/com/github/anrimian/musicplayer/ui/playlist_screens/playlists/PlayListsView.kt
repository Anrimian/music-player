package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface PlayListsView : BaseLibraryView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptySearchResult()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showErrorState(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun updateList(lists: List<PlayList>)

    @OneExecution
    fun showConfirmDeletePlayListsDialog(playLists: Collection<PlayList>)

    @OneExecution
    fun showPlayListsDeleteSuccess(playLists: Collection<PlayList>)

    @OneExecution
    fun showDeletePlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun showEditPlayListNameDialog(playList: PlayList)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

    @OneExecution
    fun launchPickFolderScreen()

    @OneExecution
    fun showPlaylistExportSuccess(playlists: List<PlayList>)

    @OneExecution
    fun launchPlayListScreen(playlistId: Long)

    @Skip
    fun onPlaylistSelected(playlist: PlayList, position: Int)

    @Skip
    fun onPlaylistUnselected(playlist: PlayList, position: Int)

    @Skip
    fun setItemsSelected(selected: Boolean)

    @AddToEndSingle
    fun showSelectionMode(playlists: Set<PlayList>)

    @OneExecution
    fun showSelectPlayListDialog(playlists: Collection<PlayList>, closeMultiselect: Boolean)

    @OneExecution
    fun sendCompositions(compositions: List<Composition>)

    @OneExecution
    fun showOverwritePlaylistDialog()

    @OneExecution
    fun showNotCompletelyImportedPlaylistDialog(playlistId: Long, notFoundFilesCount: Int)

    companion object {
        private const val LIST_STATE = "list_state"
    }
}