package com.github.anrimian.musicplayer.ui.library.common.compositions

import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.mvp.ListMvpView
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface BaseLibraryCompositionsView<C : Composition> : ListMvpView<C>, BaseLibraryView {

    @OneExecution
    fun showSelectPlayListDialog()

    @OneExecution
    fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showDeleteCompositionError(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeleteCompositionMessage(compositionsToDelete: List<DeletedComposition>)

    @Skip
    fun onCompositionSelected(composition: C, position: Int)

    @Skip
    fun onCompositionUnselected(composition: C, position: Int)

    @Skip
    fun setItemsSelected(selected: Boolean)

    @AddToEndSingle
    fun showSelectionMode(count: Int)

    @Skip
    fun shareCompositions(selectedCompositions: Collection<Composition>)

    @AddToEndSingle
    fun setDisplayCoversEnabled(isCoversEnabled: Boolean)

    @AddToEndSingle
    fun showRandomMode(isRandomModeEnabled: Boolean)

    @AddToEndSingle
    fun showCurrentComposition(currentComposition: CurrentComposition)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

    @AddToEndSingle
    fun showFilesSyncState(states: Map<Long, FileSyncState>)

}