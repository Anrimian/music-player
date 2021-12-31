package com.github.anrimian.musicplayer.ui.library.folders

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy
import moxy.MvpView
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

/**
 * Created on 23.10.2017.
 */

private const val LIST_STATE = "list_state"
private const val FOLDER_STATE = "back_path_button_state"
private const val PROGRESS_DIALOG_STATE = "progress_dialog_state"

interface LibraryFoldersView : MvpView {

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showEmptySearchResult()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showError(errorCommand: ErrorCommand)

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = FOLDER_STATE)
    fun showFolderInfo(folder: FolderFileSource)

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = FOLDER_STATE)
    fun hideFolderInfo()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = PROGRESS_DIALOG_STATE)
    fun hideProgressDialog()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = PROGRESS_DIALOG_STATE)
    fun showMoveProgress()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = PROGRESS_DIALOG_STATE)
    fun showDeleteProgress()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = PROGRESS_DIALOG_STATE)
    fun showRenameProgress()

    @OneExecution
    fun goBackToParentFolderScreen()

    @AddToEndSingle
    fun updateList(list: List<FileSource>)

    @OneExecution
    fun showSelectOrderScreen(folderOrder: Order)

    @OneExecution
    fun showSelectPlayListDialog()

    @OneExecution
    fun showAddingToPlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>)

    @OneExecution
    fun showSelectPlayListForFolderDialog(folder: FolderFileSource)

    @OneExecution
    fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showDeleteCompositionError(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeleteCompositionMessage(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showConfirmDeleteDialog(folder: FolderFileSource)

    @AddToEndSingle
    fun showSearchMode(show: Boolean)

    @OneExecution
    fun sendCompositions(compositions: List<Composition>)

    @OneExecution
    fun showReceiveCompositionsForSendError(errorCommand: ErrorCommand)

    @Skip
    fun goToMusicStorageScreen(folderId: Long)

    @Skip
    fun showCompositionActionDialog(composition: Composition)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun setDisplayCoversEnabled(isCoversEnabled: Boolean)

    @Skip
    fun showInputFolderNameDialog(folder: FolderFileSource)

    @AddToEndSingle
    fun showSelectionMode(count: Int)

    @Skip
    fun onItemSelected(item: FileSource, position: Int)

    @Skip
    fun onItemUnselected(item: FileSource, position: Int)

    @Skip
    fun setItemsSelected(selected: Boolean)

    @Skip
    fun updateMoveFilesList()

    @AddToEndSingle
    fun showMoveFileMenu(show: Boolean)

    @Skip
    fun showInputNewFolderNameDialog()

    @OneExecution
    fun showAddedIgnoredFolderMessage(folder: IgnoredFolder)

    @OneExecution
    fun onCompositionsAddedToPlayNext(compositions: List<Composition>)

    @OneExecution
    fun onCompositionsAddedToQueue(compositions: List<Composition>)

    @AddToEndSingle
    fun showCurrentComposition(currentComposition: CurrentComposition)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

}