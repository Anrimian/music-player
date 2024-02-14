package com.github.anrimian.musicplayer.ui.player_screen

import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import java.util.LinkedList

/**
 * Created on 02.11.2017.
 */
class PlayerPresenter(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playerScreenInteractor: PlayerScreenInteractor,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryPresenter<PlayerView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private var currentItem: PlayQueueItem? = null

    private var isCoversEnabled = false

    private val compositionsForPlayList = LinkedList<Composition>()

    private var lastDeleteAction: Completable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.setButtonPanelState(playerScreenInteractor.isPlayerPanelOpen)
        subscribeOnUiSettings()
        subscribeOnRandomMode()
        subscribeOnSpeedAvailableState()
        subscribeOnSpeedState()
        subscribeOnRepeatMode()
        subscribeOnPlayerStateChanges()
        subscribeOnErrorEvents()
        subscribeOnCurrentComposition()
        subscribeOnCurrentCompositionSyncState()
        subscribeOnTrackPositionChanging()
        subscribeOnSleepTimerTime()
        subscribeOnFileScannerState()
        playerScreenInteractor.playerScreensSwipeObservable
            .unsafeSubscribeOnUi(viewState::showScreensSwipeEnabled)
        playerScreenInteractor.volumeObservable.unsafeSubscribeOnUi(viewState::onVolumeChanged)
    }

    fun onSetupScreenStateRequested() {
        viewState.showDrawerScreen(
            playerScreenInteractor.selectedDrawerScreen,
            playerScreenInteractor.selectedPlayListScreenId
        )
        viewState.showPlayerContentPage(playerScreenInteractor.playerContentPage)
    }

    fun onOpenPlayerPanelClicked() {
        playerScreenInteractor.isPlayerPanelOpen = true
    }

    fun onBottomPanelExpanded() {
        playerScreenInteractor.isPlayerPanelOpen = true
        viewState.setButtonPanelState(true)
    }

    fun onBottomPanelCollapsed() {
        playerScreenInteractor.isPlayerPanelOpen = false
        viewState.setButtonPanelState(false)
    }

    fun onDrawerScreenSelected(screenId: Int) {
        playerScreenInteractor.selectedDrawerScreen = screenId
        viewState.showDrawerScreen(screenId, 0)
    }

    fun onLibraryScreenSelected() {
        viewState.showLibraryScreen(
            playerScreenInteractor.selectedLibraryScreen,
            playerScreenInteractor.selectedArtistScreenId,
            playerScreenInteractor.selectedAlbumScreenId,
            playerScreenInteractor.selectedGenreScreenId,
        )
    }

    fun onLibraryScreenSelected(screenId: Int) {
        playerScreenInteractor.selectedLibraryScreen = screenId
    }

    fun onPlayerContentPageChanged(position: Int) {
        playerScreenInteractor.playerContentPage = position
    }

    fun onPlayButtonClicked() {
        playerInteractor.play()
    }

    fun onStopButtonClicked() {
        playerInteractor.pause()
    }

    fun onSkipToPreviousButtonClicked() {
        playerInteractor.skipToPrevious()
    }

    fun onSkipToNextButtonClicked() {
        playerInteractor.skipToNext()
    }

    fun onRepeatModeChanged(mode: Int) {
        playerInteractor.setRepeatMode(mode)
    }

    fun onRandomPlayingButtonClicked(enable: Boolean) {
        playerInteractor.setRandomPlayingEnabled(enable)
    }

    fun onShareCompositionButtonClicked() {
        currentItem?.let { item -> viewState.showShareCompositionDialog(item.composition) }
    }

    fun onTrackRewoundTo(progress: Int) {
        playerInteractor.seekTo(progress.toLong())
    }

    fun onDeleteCurrentCompositionButtonClicked() {
        currentItem?.let { item -> viewState.showConfirmDeleteDialog(listOf(item.composition)) }
    }

    fun onAddCurrentCompositionToPlayListButtonClicked() {
        currentItem?.let { item ->
            compositionsForPlayList.clear()
            compositionsForPlayList.add(item.composition)
            viewState.showSelectPlayListDialog()
        }
    }

    fun onPlayListForAddingSelected(playList: PlayList) {
        addPreparedCompositionsToPlayList(playList)
    }

    fun onDeleteCompositionsDialogConfirmed(compositionsToDelete: List<Composition>) {
        deletePreparedCompositions(compositionsToDelete)
    }

    fun onSeekStart() {
        playerInteractor.onSeekStarted()
    }

    fun onSeekStop(progress: Int) {
        playerInteractor.onSeekFinished(progress.toLong())
    }

    fun onEditCompositionButtonClicked() {
        currentItem?.let { item -> viewState.startEditCompositionScreen(item.composition.id) }
    }

    fun onShowCurrentCompositionInFoldersClicked() {
        currentItem?.let { item -> viewState.locateCompositionInFolders(item.composition) }
    }

    fun onRestoreDeletedItemClicked() {
        playerInteractor.restoreDeletedItem().justSubscribe(this::onDefaultError)
    }

    fun onFastSeekForwardCalled() {
        playerInteractor.fastSeekForward()
    }

    fun onFastSeekBackwardCalled() {
        playerInteractor.fastSeekBackward()
    }

    fun onRetryFailedDeleteActionClicked() {
        if (lastDeleteAction != null) {
            lastDeleteAction!!
                .doFinally { lastDeleteAction = null }
                .justSubscribe(this::onDeleteCompositionError)
        }
    }

    fun onPlaybackSpeedSelected(speed: Float) {
        viewState.displayPlaybackSpeed(speed)
        playerInteractor.setPlaybackSpeed(speed)
    }

    private fun subscribeOnRepeatMode() {
        playerInteractor.getRepeatModeObservable().unsafeSubscribeOnUi(viewState::showRepeatMode)
    }

    private fun addPreparedCompositionsToPlayList(playList: PlayList) {
        performAddToPlaylist(compositionsForPlayList, playList) { compositionsForPlayList.clear() }
    }

    private fun deletePreparedCompositions(compositionsToDelete: List<Composition>) {
        lastDeleteAction = playerInteractor.deleteCompositions(compositionsToDelete)
            .observeOn(uiScheduler)
            .doOnSuccess(viewState::showDeleteCompositionMessage)
            .ignoreElement()
        lastDeleteAction!!.justSubscribe(this::onDeleteCompositionError)
    }

    private fun onDeleteCompositionError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun subscribeOnCurrentCompositionSyncState() {
        playerScreenInteractor.currentCompositionFileSyncState
            .unsafeSubscribeOnUi(this::onCurrentCompositionSyncStateReceived)
    }

    private fun onCurrentCompositionSyncStateReceived(fileSyncState: FileSyncState) {
        viewState.showCurrentCompositionSyncState(fileSyncState, currentItem)
    }

    private fun subscribeOnCurrentComposition() {
        playerInteractor.getCurrentQueueItemObservable()
            .unsafeSubscribeOnUi(this::onPlayQueueEventReceived)
    }

    private fun onPlayQueueEventReceived(playQueueEvent: PlayQueueEvent) {
        val newItem = playQueueEvent.playQueueItem
        val currentItem = this.currentItem

        if (currentItem == null
            || currentItem != newItem
            || !CompositionHelper.areSourcesTheSame(newItem.composition, currentItem.composition)) {

            var updateCover = false
            if ((currentItem == null) != (newItem == null)) {
                updateCover = true
            } else if (currentItem != null && newItem != null)  {
                val newComposition = newItem.composition
                val currentComposition = currentItem.composition
                updateCover = currentComposition.dateModified != newComposition.dateModified
                        || currentComposition.coverModifyTime != newComposition.coverModifyTime
                        || currentComposition.size != newComposition.size
                        || currentComposition.isFileExists != newComposition.isFileExists
            }

            this.currentItem = newItem
            viewState.showCurrentQueueItem(newItem)

            if (updateCover) {
                showCurrentItemCover(newItem)
            }
        }
    }

    private fun subscribeOnPlayerStateChanges() {
        playerInteractor.getIsPlayingStateObservable()
            .unsafeSubscribeOnUi(viewState::showPlayerState)
    }

    private fun subscribeOnErrorEvents() {
        playerInteractor.getPlayerStateObservable()
            .unsafeSubscribeOnUi(this::onPlayerStateReceived)
    }

    private fun onPlayerStateReceived(playerState: PlayerState) {
        if (playerState is PlayerState.Error) {
            if (playerState.throwable is UnavailableMediaStoreException) {
                //after lazy-prepare implementation this case can be removed.
                // Do not forget to check after remove.
                viewState.showPlayErrorState(null)
            } else {
                val errorCommand = errorParser.parseError(playerState.throwable)
                viewState.showPlayErrorState(errorCommand)
            }
        } else {
            viewState.showPlayErrorState(null)
        }
    }

    private fun subscribeOnTrackPositionChanging() {
        playerInteractor.getTrackPositionObservable()
            .unsafeSubscribeOnUi(this::onTrackPositionChanged)
    }

    private fun onTrackPositionChanged(currentPosition: Long) {
        currentItem?.let { item ->
            val duration = item.composition.duration
            viewState.showTrackState(currentPosition, duration)
        }
    }

    private fun subscribeOnUiSettings() {
        playerScreenInteractor.coversEnabledObservable
            .subscribeOnUi(this::onUiSettingsReceived, errorParser::logError)
    }

    private fun onUiSettingsReceived(isCoversEnabled: Boolean) {
        this.isCoversEnabled = isCoversEnabled
        showCurrentItemCover(currentItem)
    }

    private fun showCurrentItemCover(item: PlayQueueItem?) {
        val currentItem = if (isCoversEnabled) item else null
        viewState.showCurrentItemCover(currentItem)
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun subscribeOnRandomMode() {
        playerInteractor.getRandomPlayingObservable()
            .unsafeSubscribeOnUi(viewState::showRandomPlayingButton)
    }

    private fun subscribeOnSpeedAvailableState() {
        playerInteractor.getSpeedChangeAvailableObservable()
            .unsafeSubscribeOnUi(viewState::showSpeedChangeFeatureVisible)
    }

    private fun subscribeOnSpeedState() {
        playerInteractor.getPlaybackSpeedObservable()
            .unsafeSubscribeOnUi(viewState::displayPlaybackSpeed)
    }

    private fun subscribeOnSleepTimerTime() {
        playerScreenInteractor.sleepTimerCountDownObservable
            .unsafeSubscribeOnUi(viewState::showSleepTimerRemainingTime)
    }

    private fun subscribeOnFileScannerState() {
        playerScreenInteractor.fileScannerStateObservable
            .unsafeSubscribeOnUi(viewState::showFileScannerState)
    }
}