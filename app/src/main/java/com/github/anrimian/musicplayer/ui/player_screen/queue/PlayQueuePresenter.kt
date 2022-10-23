package com.github.anrimian.musicplayer.ui.player_screen.queue

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
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

class PlayQueuePresenter(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlayListsInteractor,
    private val playerScreenInteractor: PlayerScreenInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
): AppPresenter<PlayQueueView>(uiScheduler, errorParser) {

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
        subscribeOnUiSettings()
    }

    fun onStart() {
        subscribeOnPlayerStateChanges()
        subscribeOnPlayQueue()
        subscribeOnCurrentCompositionChanging()
        subscribeOnCurrentPosition()
    }

    fun onStop() {
        batterySafeDisposable.clear()
    }


    fun onQueueItemClicked(position: Int, item: PlayQueueItem) {
        if (item == currentItem) {
            playerInteractor.playOrPause()
            return
        }
        currentPosition = position
        currentItem = item
        playerInteractor.skipToItem(item.id)
        viewState.showCurrentQueueItem(currentItem, isCoversEnabled)
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
                .subscribe({}, this::onDeleteCompositionError)
        }
    }

    fun onRestoreDeletedItemClicked() {
        playerInteractor.restoreDeletedItem().justSubscribe(this::onDefaultError)
    }

    fun onPlayListForAddingCreated(playList: PlayList?) {
        val compositionsToAdd = playQueue.map(PlayQueueItem::getComposition)
        playListsInteractor.addCompositionsToPlayList(compositionsToAdd, playList)
            .subscribeOnUi(
                { viewState.showAddingToPlayListComplete(playList, compositionsToAdd) },
                this::onAddingToPlayListError
            )
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
        playerInteractor.swapItems(fromItem, toItem)
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

    private fun deletePlayQueueItem(item: PlayQueueItem) {
        playerInteractor.removeQueueItem(item).unsafeSubscribeOnUi(viewState::showDeletedItemMessage)
    }

    private fun addPreparedCompositionsToPlayList(playList: PlayList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
            .subscribeOnUi({ onAddingToPlayListCompleted(playList) }, this::onAddingToPlayListError)
    }

    private fun onAddingToPlayListError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showAddingToPlayListError(errorCommand)
    }

    private fun onAddingToPlayListCompleted(playList: PlayList) {
        viewState.showAddingToPlayListComplete(playList, compositionsForPlayList)
        compositionsForPlayList.clear()
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

    private fun subscribeOnPlayerStateChanges() {
        batterySafeDisposable.add(playerInteractor.getIsPlayingStateObservable()
            .observeOn(uiScheduler)
            .subscribe(viewState::showPlayerState))
    }

    private fun subscribeOnPlayQueue() {
        batterySafeDisposable.add(playerInteractor.getPlayQueueObservable()
            .observeOn(uiScheduler)
            .filter(listDragFilter::filterListEmitting)
            .subscribe(this::onPlayQueueChanged, errorParser::logError))
    }

    private fun onPlayQueueChanged(list: List<PlayQueueItem>) {
        playQueue = list
        viewState.updatePlayQueue(list)
    }

    private fun subscribeOnCurrentCompositionChanging() {
        batterySafeDisposable.add(playerInteractor.getCurrentQueueItemObservable()
            .observeOn(uiScheduler)
            .subscribe(this::onPlayQueueEventReceived))
    }

    private fun subscribeOnCurrentPosition() {
        batterySafeDisposable.add(playerInteractor.getCurrentItemPositionObservable()
            .observeOn(uiScheduler)
            .subscribe(this::onItemPositionReceived))
    }

    private fun onItemPositionReceived(position: Int) {
        if (!isDragging && currentPosition != position) {
            currentPosition = position
            viewState.scrollQueueToPosition(position, playerScreenInteractor.isPlayerPanelOpen)
        }
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

}