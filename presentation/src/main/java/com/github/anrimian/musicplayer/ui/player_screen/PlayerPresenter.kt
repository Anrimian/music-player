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
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ListDragFilter
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

    private val listDragFilter = ListDragFilter()

    private var playQueue: List<PlayQueueItem> = ArrayList()
    private var currentItem: PlayQueueItem? = null
    private var currentPosition = -1
    
    private var isDragging = false
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
        subscribeOnPlayQueue()
        subscribeOnCurrentCompositionChanging()
        subscribeOnCurrentPosition()
        subscribeOnTrackPositionChanging()
        subscribeOnSleepTimerTime()
    }

    fun onStop() {
        batterySafeDisposable.clear()
    }

    fun onCurrentScreenRequested() {
        viewState.showDrawerScreen(playerScreenInteractor.selectedDrawerScreen,
                playerScreenInteractor.selectedPlayListScreenId)
    }

    fun onOpenPlayQueueClicked() {
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
        playerInteractor.repeatMode = mode
    }

    fun onRandomPlayingButtonClicked(enable: Boolean) {
        playerInteractor.isRandomPlayingEnabled = enable
    }

    fun onShareCompositionButtonClicked() {
        viewState.showShareMusicDialog(currentItem!!.composition)
    }

    fun onQueueItemClicked(position: Int, item: PlayQueueItem) {
        if (item == currentItem) {
            playerInteractor.playOrPause()
            return
        }
        currentPosition = position
        currentItem = item
        playerInteractor.skipToItem(item)
        onCurrentCompositionChanged(item, 0)
    }

    fun onQueueItemIconClicked(position: Int, playQueueItem: PlayQueueItem) {
        if (playQueueItem == currentItem) {
            playerInteractor.playOrPause()
        } else {
            onQueueItemClicked(position, playQueueItem)
            playerInteractor.play()
        }
    }

    fun onTrackRewoundTo(progress: Int) {
        playerInteractor.seekTo(progress.toLong())
    }

    fun onDeleteCompositionButtonClicked(composition: Composition) {
        viewState.showConfirmDeleteDialog(listOf(composition))
    }

    fun onDeleteCurrentCompositionButtonClicked() {
        viewState.showConfirmDeleteDialog(listOf(currentItem!!.composition))
    }

    fun onAddQueueItemToPlayListButtonClicked(composition: Composition) {
        compositionsForPlayList.clear()
        compositionsForPlayList.add(composition)
        viewState.showSelectPlayListDialog()
    }

    fun onAddCurrentCompositionToPlayListButtonClicked() {
        compositionsForPlayList.clear()
        compositionsForPlayList.add(currentItem!!.composition)
        viewState.showSelectPlayListDialog()
    }

    fun onPlayListForAddingSelected(playList: PlayList) {
        addPreparedCompositionsToPlayList(playList)
    }

    fun onPlayListForAddingCreated(playList: PlayList?) {
        val compositionsToAdd = playQueue.map(PlayQueueItem::getComposition)
        playListsInteractor.addCompositionsToPlayList(compositionsToAdd, playList)
                .subscribeOnUi(
                        { viewState.showAddingToPlayListComplete(playList, compositionsToAdd) },
                        this::onAddingToPlayListError
                )
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

    fun onItemSwipedToDelete(position: Int) {
        deletePlayQueueItem(playQueue[position])
    }

    fun onDeleteQueueItemClicked(item: PlayQueueItem?) {
        deletePlayQueueItem(item)
    }

    fun onItemMoved(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                swapItems(i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                swapItems(i, i - 1)
            }
        }
    }

    fun onEditCompositionButtonClicked() {
        viewState.startEditCompositionScreen(currentItem!!.composition.id)
    }

    fun onRestoreDeletedItemClicked() {
        playerInteractor.restoreDeletedItem().justSubscribe(this::onDefaultError)
    }

    fun onClearPlayQueueClicked() {
        playerInteractor.clearPlayQueue()
    }

    fun onFastSeekForwardCalled() {
        playerInteractor.fastSeekForward()
    }

    fun onFastSeekBackwardCalled() {
        playerInteractor.fastSeekBackward()
    }

    fun onDragStarted() {
        isDragging = true
    }

    fun onDragEnded() {
        isDragging = false
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
        playerInteractor.playbackSpeed = speed
    }

    private fun swapItems(from: Int, to: Int) {
        if (!ListUtils.isIndexInRange(playQueue, from) || !ListUtils.isIndexInRange(playQueue, to)) {
            return
        }
        val fromItem = playQueue[from]
        val toItem = playQueue[to]
        Collections.swap(playQueue, from, to)
        viewState.notifyItemMoved(from, to)

        listDragFilter.increaseEventsToSkip()
        playerInteractor.swapItems(fromItem, toItem)
    }

    private fun deletePlayQueueItem(item: PlayQueueItem?) {
        playerInteractor.removeQueueItem(item).unsafeSubscribeOnUi(viewState::showDeletedItemMessage)
    }

    private fun subscribeOnRepeatMode() {
        batterySafeDisposable.add(playerInteractor.repeatModeObservable
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

    private fun subscribeOnCurrentCompositionChanging() {
        batterySafeDisposable.add(playerInteractor.currentQueueItemObservable
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueEventReceived))
    }

    private fun subscribeOnCurrentPosition() {
        batterySafeDisposable.add(playerInteractor.currentItemPositionObservable
                .observeOn(uiScheduler)
                .subscribe(this::onItemPositionReceived))
    }

    private fun onItemPositionReceived(position: Int) {
        if (!isDragging && currentPosition != position) {
            currentPosition = position
            viewState.scrollQueueToPosition(position)
        }
    }

    private fun onPlayQueueEventReceived(playQueueEvent: PlayQueueEvent) {
        val newItem = playQueueEvent.playQueueItem
        if (currentItem == null || currentItem != newItem
                || !CompositionHelper.areSourcesTheSame(newItem!!.composition, currentItem!!.composition)) {
            onCurrentCompositionChanged(newItem, playQueueEvent.trackPosition)
        }
    }

    private fun onCurrentCompositionChanged(newItem: PlayQueueItem?, trackPosition: Long) {
        viewState.showCurrentQueueItem(newItem, isCoversEnabled)
        if (newItem != null
                && (newItem != currentItem || newItem.composition.duration != currentItem!!.composition.duration)) {
            viewState.showTrackState(trackPosition, newItem.composition.duration)
        }
        currentItem = newItem
    }

    private fun subscribeOnPlayerStateChanges() {
        batterySafeDisposable.add(playerInteractor.playerStateObservable
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged))
    }

    private fun onPlayerStateChanged(playerState: PlayerState) {
        viewState.showPlayerState(playerState)
    }

    private fun subscribeOnPlayQueue() {
        batterySafeDisposable.add(playerInteractor.playQueueObservable
                .observeOn(uiScheduler)
                .filter(listDragFilter::filterListEmitting)
                .subscribe(this::onPlayQueueChanged, this::onPlayQueueReceivingError))
    }

    private fun onPlayQueueChanged(list: List<PlayQueueItem>) {
        playQueue = list
        viewState.showPlayQueueSubtitle(playQueue.size)
        viewState.setMusicControlsEnabled(playQueue.isNotEmpty())
        viewState.updatePlayQueue(list)
    }

    private fun onPlayQueueReceivingError(throwable: Throwable) {
        errorParser.parseError(throwable)
        viewState.setMusicControlsEnabled(false)
    }

    private fun subscribeOnTrackPositionChanging() {
        batterySafeDisposable.add(playerInteractor.trackPositionObservable
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged))
    }

    private fun onTrackPositionChanged(currentPosition: Long) {
        if (currentItem != null) {
            val duration = currentItem!!.composition.duration
            viewState.showTrackState(currentPosition, duration)
        }
    }

    private fun subscribeOnUiSettings() {
        playerScreenInteractor.coversEnabledObservable
                .subscribeOnUi(this::onUiSettingsReceived, errorParser::logError)
    }

    private fun onUiSettingsReceived(isCoversEnabled: Boolean) {
        this.isCoversEnabled = isCoversEnabled
        viewState.setPlayQueueCoversEnabled(isCoversEnabled)
        if (currentItem != null) {
            viewState.showCurrentQueueItem(currentItem, isCoversEnabled)
        }
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun subscribeOnRandomMode() {
        playerInteractor.randomPlayingObservable
                .unsafeSubscribeOnUi(viewState::showRandomPlayingButton)
    }

    private fun subscribeOnSpeedAvailableState() {
        playerInteractor.speedChangeAvailableObservable
                .unsafeSubscribeOnUi(viewState::showSpeedChangeFeatureVisible)
    }

    private fun subscribeOnSpeedState() {
        playerInteractor.playbackSpeedObservable
                .unsafeSubscribeOnUi(viewState::displayPlaybackSpeed)
    }

    private fun subscribeOnSleepTimerTime() {
        batterySafeDisposable.add(playerScreenInteractor.sleepTimerCountDownObservable
                .observeOn(uiScheduler)
                .subscribe(viewState::showSleepTimerRemainingTime))
    }
}