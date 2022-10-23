package com.github.anrimian.musicplayer.ui.player_screen

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
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.*

/**
 * Created on 02.11.2017.
 */
class PlayerPresenter(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlayListsInteractor,
    private val playerScreenInteractor: PlayerScreenInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : AppPresenter<PlayerView>(uiScheduler, errorParser) {

    private val batterySafeDisposable = CompositeDisposable()

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
    }

    fun onStart() {
        subscribeOnRepeatMode()
        subscribeOnPlayerStateChanges()
        subscribeOnErrorEvents()
        subscribeOnCurrentCompositionChanging()
        subscribeOnCurrentCompositionSyncState()
        subscribeOnTrackPositionChanging()
        subscribeOnSleepTimerTime()
        subscribeOnFileScannerState()
    }

    fun onStop() {
        batterySafeDisposable.clear()
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
        viewState.showLibraryScreen(playerScreenInteractor.selectedLibraryScreen)
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

    fun onDeleteCompositionButtonClicked(composition: Composition) {
        viewState.showConfirmDeleteDialog(listOf(composition))
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
                .subscribe({}, this::onDeleteCompositionError)
        }
    }

    fun onPlaybackSpeedSelected(speed: Float) {
        viewState.displayPlaybackSpeed(speed)
        playerInteractor.setPlaybackSpeed(speed)
    }

    private fun subscribeOnRepeatMode() {
        batterySafeDisposable.add(playerInteractor.getRepeatModeObservable()
            .observeOn(uiScheduler)
            .subscribe(viewState::showRepeatMode))
    }

    private fun addPreparedCompositionsToPlayList(playList: PlayList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
            .subscribeOnUi({ onAddingToPlayListCompleted(playList) }, this::onAddingToPlayListError)
    }

    private fun deletePreparedCompositions(compositionsToDelete: List<Composition>) {
        lastDeleteAction = playerInteractor.deleteCompositions(compositionsToDelete)
            .observeOn(uiScheduler)
            .doOnComplete { onDeleteCompositionsSuccess(compositionsToDelete) }
        lastDeleteAction!!.justSubscribe(this::onDeleteCompositionError)
    }

    private fun onDeleteCompositionsSuccess(compositionsToDelete: List<Composition>) {
        viewState.showDeleteCompositionMessage(compositionsToDelete)
    }

    private fun onDeleteCompositionError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun onAddingToPlayListError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showAddingToPlayListError(errorCommand)
    }

    private fun onAddingToPlayListCompleted(playList: PlayList) {
        viewState.showAddingToPlayListComplete(playList, compositionsForPlayList)
        compositionsForPlayList.clear()
    }

    private fun subscribeOnCurrentCompositionSyncState() {
        batterySafeDisposable.add(playerScreenInteractor.currentCompositionFileSyncState
            .observeOn(uiScheduler)
            .subscribe(viewState::showCurrentCompositionSyncState))
    }

    private fun subscribeOnCurrentCompositionChanging() {
        batterySafeDisposable.add(playerInteractor.getCurrentQueueItemObservable()
            .observeOn(uiScheduler)
            .subscribe(this::onPlayQueueEventReceived))
    }

    private fun onPlayQueueEventReceived(playQueueEvent: PlayQueueEvent) {
        val newItem = playQueueEvent.playQueueItem
        if (currentItem == null
            || currentItem != newItem
            || !CompositionHelper.areSourcesTheSame(newItem!!.composition, currentItem!!.composition)) {
            currentItem = newItem
            viewState.showCurrentQueueItem(newItem, isCoversEnabled)
        }
    }

    private fun subscribeOnPlayerStateChanges() {
        batterySafeDisposable.add(playerInteractor.getIsPlayingStateObservable()
            .observeOn(uiScheduler)
            .subscribe(viewState::showPlayerState))
    }

    private fun subscribeOnErrorEvents() {
        batterySafeDisposable.add(playerInteractor.getPlayerStateObservable()
            .observeOn(uiScheduler)
            .subscribe(this::onPlayerStateReceived))
    }

    private fun onPlayerStateReceived(playerState: PlayerState) {
        if (playerState is PlayerState.Error) {
            val errorCommand = errorParser.parseError(playerState.throwable)
            viewState.showPlayErrorState(errorCommand)
        } else {
            viewState.showPlayErrorState(null)
        }
    }

    private fun subscribeOnTrackPositionChanging() {
        batterySafeDisposable.add(playerInteractor.getTrackPositionObservable()
            .observeOn(uiScheduler)
            .subscribe(this::onTrackPositionChanged))
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
        currentItem?.let { item -> viewState.showCurrentQueueItem(item, isCoversEnabled) }
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
        batterySafeDisposable.add(playerScreenInteractor.sleepTimerCountDownObservable
            .observeOn(uiScheduler)
            .subscribe(viewState::showSleepTimerRemainingTime))
    }

    private fun subscribeOnFileScannerState() {
        batterySafeDisposable.add(playerScreenInteractor.fileScannerStateObservable
            .observeOn(uiScheduler)
            .subscribe(viewState::showFileScannerState))
    }
}