package com.github.anrimian.musicplayer.ui.player_screen

import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

/**
 * Created on 02.11.2017.
 */
interface PlayerView : BaseLibraryView {
    
    @AddToEndSingle
    fun showPlayerState(isPlaying: Boolean)

    @AddToEndSingle
    fun showPlayErrorState(errorCommand: ErrorCommand?)

    @AddToEndSingle
    fun setButtonPanelState(expanded: Boolean)

    @OneExecution
    fun showPlayerContentPage(position: Int)

    @AddToEndSingle
    fun showCurrentQueueItem(item: PlayQueueItem?)

    @AddToEndSingle
    fun showCurrentItemCover(item: PlayQueueItem?)

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
    fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>)

    @OneExecution
    fun showDeleteCompositionError(errorCommand: ErrorCommand)

    @OneExecution
    fun showDeleteCompositionMessage(compositionsToDelete: List<DeletedComposition>)

    @OneExecution
    fun showDrawerScreen(selectedDrawerScreenId: Int, selectedPlayListScreenId: Long)

    @OneExecution
    fun showLibraryScreen(
        selectedLibraryScreen: Int,
        selectedArtistScreenId: Long,
        selectedAlbumScreenId: Long,
        selectedGenreScreenId: Long
    )

    @Skip
    fun startEditCompositionScreen(id: Long)

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
    fun showCurrentCompositionSyncState(syncState: FileSyncState, item: PlayQueueItem?)

    @AddToEndSingle
    fun showScreensSwipeEnabled(enabled: Boolean)

    @AddToEndSingle
    fun onVolumeChanged(volume: VolumeState)

}