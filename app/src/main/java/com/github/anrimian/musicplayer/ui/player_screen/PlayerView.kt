package com.github.anrimian.musicplayer.ui.player_screen

import com.github.anrimian.filesync.models.state.file.FormattedFileSyncState
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

/**
 * Created on 02.11.2017.
 */
interface PlayerView : MvpView {
    
    @AddToEndSingle
    fun showPlayerState(isPlaying: Boolean)

    @AddToEndSingle
    fun showPlayErrorState(errorCommand: ErrorCommand?)

    @AddToEndSingle
    fun setButtonPanelState(expanded: Boolean)

    @OneExecution
    fun showPlayerContentPage(position: Int)

    @AddToEndSingle
    fun showCurrentQueueItem(item: PlayQueueItem?, showCover: Boolean)

    @AddToEndSingle
    fun showRepeatMode(mode: Int)

    @AddToEndSingle
    fun showRandomPlayingButton(active: Boolean)

    @AddToEndSingle
    fun showTrackState(currentPosition: Long, duration: Long)

    @OneExecution
    fun showSelectPlayListDialog()

    @OneExecution
    fun showShareCompositionDialog(composition: Composition)

    @OneExecution
    fun showAddingToPlayListError(errorCommand: ErrorCommand)

    @OneExecution
    fun showAddingToPlayListComplete(playList: PlayList?, compositions: List<Composition>)

    @OneExecution
    fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showDeleteCompositionError(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeleteCompositionMessage(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showDrawerScreen(selectedDrawerScreenId: Int, selectedPlayListScreenId: Long)

    @OneExecution
    fun showLibraryScreen(selectedLibraryScreen: Int)

    @Skip
    fun startEditCompositionScreen(id: Long)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeletedItemMessage()

    @AddToEndSingle
    fun displayPlaybackSpeed(speed: Float)

    @AddToEndSingle
    fun showSpeedChangeFeatureVisible(visible: Boolean)

    @AddToEndSingle
    fun showSleepTimerRemainingTime(remainingMillis: Long)

    @AddToEndSingle
    fun showFileScannerState(state: FileScannerState)

    @Skip
    fun locateCompositionInFolders(composition: Composition)

    @AddToEndSingle
    fun showCurrentCompositionSyncState(fileSyncState: FormattedFileSyncState)

}