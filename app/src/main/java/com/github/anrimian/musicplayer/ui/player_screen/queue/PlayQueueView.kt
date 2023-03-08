package com.github.anrimian.musicplayer.ui.player_screen.queue

import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleOneExecution
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val LIST_STATE = "list_state"

interface PlayQueueView : MvpView {

    @AddToEndSingle
    fun showPlayerState(isPlaying: Boolean)

    @AddToEndSingle
    fun showCurrentQueueItem(item: PlayQueueItem?)

    @OneExecution
    fun showSelectPlayListDialog()

    @OneExecution
    fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showDeleteCompositionMessage(compositionsToDelete: List<Composition>)

    @Skip
    fun notifyItemMoved(from: Int, to: Int)

    @OneExecution
    fun showDeleteCompositionError(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeletedItemMessage()

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun showAddingToPlayListComplete(playList: PlayList?, compositions: List<Composition>)

    @OneExecution
    fun showAddingToPlayListError(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun setPlayQueueCoversEnabled(isCoversEnabled: Boolean)

    @StateStrategyType(AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showList(itemsCount: Int)

    @StateStrategyType(AddToEndSingleTagStrategy::class, tag = LIST_STATE)
    fun showListError(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun updatePlayQueue(items: List<PlayQueueItem>?)

    @SingleOneExecution
    fun scrollQueueToPosition(position: Int, isSmoothScrollAllowed: Boolean)

    @AddToEndSingle
    fun showFilesSyncState(states: Map<Long, FileSyncState>)

}