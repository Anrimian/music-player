package com.github.anrimian.musicplayer.ui.player_screen.queue

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

interface PlayQueueView : MvpView {

    @AddToEndSingle
    fun showPlayerState(isPlaying: Boolean)

    @AddToEndSingle
    fun showCurrentQueueItem(item: PlayQueueItem?, showCover: Boolean)

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

    @AddToEndSingle
    fun updatePlayQueue(items: List<PlayQueueItem>)

    @OneExecution
    fun scrollQueueToPosition(position: Int, isSmoothScrollAllowed: Boolean)

}