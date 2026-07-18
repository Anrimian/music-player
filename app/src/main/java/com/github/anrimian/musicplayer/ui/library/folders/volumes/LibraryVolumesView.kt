package com.github.anrimian.musicplayer.ui.library.folders.volumes

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface LibraryVolumesView : BaseLibraryView {

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @StateStrategyType(value = AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showError(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun updateList(list: List<Volume>)

    @OneExecution
    fun showSelectPlayListDialog()

    @OneExecution
    fun showSelectPlayListFromVolumeDialog(volume: Volume)

    @OneExecution
    fun sendCompositions(compositions: List<Composition>)

    @Skip
    fun goToFolderScreen(folderId: Long)

    @AddToEndSingle
    fun showRandomMode(isRandomModeEnabled: Boolean)

    @AddToEndSingle
    fun showSelectionMode(count: Int)

    @Skip
    fun onItemSelected(item: Volume, position: Int)

    @Skip
    fun onItemUnselected(item: Volume, position: Int)

    @Skip
    fun setItemsSelected(selected: Boolean)

    @OneExecution
    fun showAddedIgnoredFolderMessage(folder: IgnoredFolder)

    private companion object {
        const val LIST_STATE = "list_state"
    }

}