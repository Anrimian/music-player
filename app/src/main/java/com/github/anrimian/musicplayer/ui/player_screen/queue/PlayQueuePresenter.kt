package com.github.anrimian.musicplayer.ui.player_screen.queue

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.data.utils.rx.retryWithDelay
import com.github.anrimian.musicplayer.domain.Constants.NO_POSITION
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueData
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ListDragFilter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Collections
import java.util.LinkedList
import java.util.concurrent.TimeUnit

class PlayQueuePresenter(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playerScreenInteractor: PlayerScreenInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
): BaseLibraryPresenter<PlayQueueView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private val listDragFilter = ListDragFilter()

    private var currentQueueData: PlayQueueData? = null
    private var playQueueDisposable: Disposable? = null
    private var positionDisposable: Disposable? = null

    private var playQueue: List<PlayQueueItem> = ArrayList()
    private var currentItem: PlayQueueItem? = null
    private var currentPosition = NO_POSITION

    private var isDragging = false
    private var isCoversEnabled = false

    private val compositionsForPlayList = LinkedList<Composition>()

    private var lastDeleteAction: Completable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnUiSettings()
        launchPlayQueueSubscription()
        subscribeOnPlayerStateChanges()
        subscribeOnCurrentCompositionChanging()
        syncInteractor.getFilesSyncStateObservable()
            .unsafeSubscribeOnUi(viewState::showFilesSyncState)
    }

    fun onLoadAgainQueueClicked() {
        subscribeOnPlayQueue()
    }

    fun onQueueItemClicked(position: Int, item: PlayQueueItem) {
        if (item == currentItem) {
            playerInteractor.playOrPause()
            return
        }
        currentPosition = position
        currentItem = item
        playerInteractor.skipToItem(item.itemId)
        viewState.showCurrentQueueItem(currentItem)
    }

    fun onQueueItemIconClicked(position: Int, playQueueItem: PlayQueueItem) {
        if (playQueueItem == currentItem) {
            playerInteractor.playOrPause()
        } else {
            onQueueItemClicked(position, playQueueItem)
            playerInteractor.play()
        }
    }

    fun onAddQueueItemToPlayListButtonClicked(composition: Composition) {
        compositionsForPlayList.clear()
        compositionsForPlayList.add(composition)
        viewState.showSelectPlayListDialog()
    }

    fun onPlayListForAddingSelected(playList: PlayList) {
        addPreparedCompositionsToPlayList(playList)
    }

    fun onDeleteQueueItemClicked(item: PlayQueueItem) {
        deletePlayQueueItem(item)
    }

    fun onDeleteCompositionButtonClicked(composition: Composition) {
        viewState.showConfirmDeleteDialog(listOf(composition))
    }

    fun onItemSwipedToDelete(position: Int) {
        deletePlayQueueItem(playQueue[position])
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

    fun onDragStarted() {
        isDragging = true
    }

    fun onDragEnded() {
        isDragging = false
    }

    fun onDeleteCompositionsDialogConfirmed(compositionsToDelete: List<Composition>) {
        deletePreparedCompositions(compositionsToDelete)
    }

    fun onRetryFailedDeleteActionClicked() {
        if (lastDeleteAction != null) {
            lastDeleteAction!!
                .doFinally { lastDeleteAction = null }
                .justSubscribe(this::onDeleteCompositionError)
        }
    }

    fun onRestoreDeletedItemClicked() {
        playerInteractor.restoreDeletedItem().justSubscribe(this::onDefaultError)
    }

    fun onPlayListForAddingCreated(playList: PlayList) {
        val compositionsToAdd = ArrayList(playQueue)
        performAddToPlaylist(compositionsToAdd, playList) {}
    }

    fun onClearPlayQueueClicked() {
        playerInteractor.clearPlayQueue().subscribe()
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
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
        playerInteractor.swapItems(fromItem, toItem).justSubscribe(this::onDefaultError)
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

    private fun deletePlayQueueItem(item: PlayQueueItem) {
        playerInteractor.removeQueueItem(item).unsafeSubscribeOnUi(viewState::showDeletedItemMessage)
    }

    private fun addPreparedCompositionsToPlayList(playList: PlayList) {
        performAddToPlaylist(compositionsForPlayList, playList) { compositionsForPlayList.clear() }
    }

    private fun subscribeOnUiSettings() {
        playerScreenInteractor.coversEnabledObservable
            .subscribeOnUi(this::onUiSettingsReceived, errorParser::logError)
    }

    private fun onUiSettingsReceived(isCoversEnabled: Boolean) {
        this.isCoversEnabled = isCoversEnabled
        viewState.setPlayQueueCoversEnabled(isCoversEnabled)
    }

    private fun subscribeOnPlayerStateChanges() {
        playerInteractor.getIsPlayingStateObservable()
            .unsafeSubscribeOnUi(viewState::showPlayerState)
    }

    private fun launchPlayQueueSubscription() {
        playerScreenInteractor.playQueueDataObservable
            .unsafeSubscribeOnUi(this::onQueueDataReceived)
    }

    private fun onQueueDataReceived(data: PlayQueueData) {
        if (currentQueueData != data) {
            //fast clear list to prevent list update calculation for new queues
            if (currentQueueData != null) {
                viewState.updatePlayQueue(null)
            }
            currentQueueData = data
            subscribeOnPlayQueue()
        }
    }

    private fun subscribeOnPlayQueue() {
        RxUtils.dispose(positionDisposable, presenterDisposable)
        RxUtils.dispose(playQueueDisposable, presenterDisposable)
        playQueueDisposable = playerInteractor.getPlayQueueObservable()
            .observeOn(uiScheduler)
            .filter(listDragFilter::filterListEmitting)
            .subscribe(this::onPlayQueueReceived, this::onPlayQueueReceivingError)
        presenterDisposable.add(playQueueDisposable!!)
    }

    private fun onPlayQueueReceived(list: List<PlayQueueItem>) {
        playQueue = list
        viewState.updatePlayQueue(list)
        viewState.showList(list.size)
        subscribeOnCurrentPosition()
    }

    private fun onPlayQueueReceivingError(throwable: Throwable) {
        viewState.showListError(errorParser.parseError(throwable))
    }

    private fun subscribeOnCurrentPosition() {
        if (RxUtils.isActive(positionDisposable)) {
            return
        }
        currentPosition = NO_POSITION
        positionDisposable = playerInteractor.getCurrentItemPositionObservable()
            .observeOn(uiScheduler)
            .retryWithDelay(10, 10, TimeUnit.SECONDS)
            .subscribe(
                this::onItemPositionReceived,
                { t -> viewState.showErrorMessage(errorParser.parseError(t)) }
            )
        presenterDisposable.add(positionDisposable!!)
    }

    private fun onItemPositionReceived(position: Int) {
        val firstReceive = currentPosition == NO_POSITION
        if (!isDragging && currentPosition != position) {
            currentPosition = position
            viewState.scrollQueueToPosition(position, !firstReceive)
        }
    }

    private fun subscribeOnCurrentCompositionChanging() {
        playerInteractor.getCurrentQueueItemObservable()
            .unsafeSubscribeOnUi(this::onPlayQueueEventReceived)
    }

    private fun onPlayQueueEventReceived(playQueueEvent: PlayQueueEvent) {
        val newItem = playQueueEvent.playQueueItem
        if (currentItem != newItem) {
            currentItem = newItem
            viewState.showCurrentQueueItem(newItem)
        }
    }

}