package com.github.anrimian.musicplayer.ui.playlist_screens.playlist

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ListDragFilter
import com.github.anrimian.musicplayer.ui.utils.wrappers.DeferredObject2
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

class PlayListPresenter(private val playListId: Long,
                        private val playerInteractor: LibraryPlayerInteractor,
                        private val playListsInteractor: PlayListsInteractor,
                        private val displaySettingsInteractor: DisplaySettingsInteractor,
                        errorParser: ErrorParser,
                        uiScheduler: Scheduler)
    : AppPresenter<PlayListView>(uiScheduler, errorParser) {

    private val presenterBatterySafeDisposable = CompositeDisposable()

    private val listDragFilter = ListDragFilter()

    private var currentItemDisposable: Disposable? = null
    private var itemsDisposable: Disposable? = null

    private var items: List<PlayListItem> = ArrayList()
    private var playList = DeferredObject2<PlayList>()

    private val compositionsForPlayList: MutableList<Composition> = LinkedList()
    private val compositionsToDelete: MutableList<Composition> = LinkedList()

    private var startDragPosition = 0
    private var currentItem: PlayQueueItem? = null

    private var lastDeleteAction: Completable? = null

    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnCompositions()
        subscribePlayList()
        subscribeOnCurrentComposition()
        subscribeOnRepeatMode()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenterDisposable.dispose()
    }

    fun onStart() {
        if (items.isNotEmpty()) {
            subscribeOnCurrentComposition()
        }
    }

    fun onStop(listPosition: ListPosition) {
        playListsInteractor.saveListPosition(listPosition)
        presenterBatterySafeDisposable.clear()
    }

    fun onItemIconClicked(position: Int) {
        playerInteractor.startPlaying(items.map(PlayListItem::getComposition), position)
    }

    fun onPlayAllButtonClicked() {
        playerInteractor.startPlaying(items.map(PlayListItem::getComposition))
    }

    fun onDeleteCompositionButtonClicked(composition: Composition) {
        compositionsToDelete.clear()
        compositionsToDelete.add(composition)
        viewState.showConfirmDeleteDialog(compositionsToDelete)
    }

    fun onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions()
    }

    fun onAddToPlayListButtonClicked(composition: Composition) {
        compositionsForPlayList.clear()
        compositionsForPlayList.add(composition)
        viewState.showSelectPlayListDialog()
    }

    fun onPlayNextCompositionClicked(composition: Composition) {
        addCompositionsToPlayNext(listOf(composition))
    }

    fun onAddToQueueCompositionClicked(composition: Composition) {
        addCompositionsToEnd(listOf(composition))
    }

    fun onPlayListToAddingSelected(playList: PlayList) {
        addPreparedCompositionsToPlayList(playList)
    }

    fun onDeleteFromPlayListButtonClicked(playListItem: PlayListItem) {
        deleteItem(playListItem)
    }

    fun onDeletePlayListButtonClicked() {
        playList.call(viewState::showConfirmDeletePlayListDialog)
    }

    fun onDeletePlayListDialogConfirmed(playList: PlayList) {
        playListsInteractor.deletePlayList(playList.id)
            .subscribeOnUi({ onPlayListDeleted(playList) }, this::onPlayListDeletingError)
    }

    fun onFragmentMovedToTop() {
        playListsInteractor.setSelectedPlayListScreen(playListId)
    }

    fun onItemSwipedToDelete(position: Int) {
        deleteItem(items[position])
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

    fun onDragStarted(position: Int) {
        startDragPosition = position
    }

    fun onDragEnded(position: Int) {
        if (!ListUtils.isIndexInRange(items, startDragPosition) || !ListUtils.isIndexInRange(items, position)) {
            return
        }
        playList.call { playList ->
            listDragFilter.increaseEventsToSkip()
            playListsInteractor.moveItemInPlayList(playList, startDragPosition, position)
                .justSubscribe(this::onDefaultError)
        }
    }

    fun onPlayActionSelected(position: Int) {
        playerInteractor.startPlaying(items.map(PlayListItem::getComposition), position)
    }

    fun onRestoreRemovedItemClicked() {
        playListsInteractor.restoreDeletedPlaylistItem().justSubscribe(this::onDefaultError)
    }

    fun onChangePlayListNameButtonClicked() {
        playList.call(viewState::showEditPlayListNameDialog)
    }

    fun onSearchTextChanged(text: String?) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text
            viewState.setDragEnabled(searchText.isNullOrEmpty())
            subscribeOnCompositions()
        }
    }

    fun onRetryFailedDeleteActionClicked() {
        if (lastDeleteAction != null) {
            lastDeleteAction!!
                .doFinally { lastDeleteAction = null }
                .justSubscribe(this::onDeleteCompositionsError)
        }
    }

    fun onChangeRandomModePressed() {
        playerInteractor.setRandomPlayingEnabled(!playerInteractor.isRandomPlayingEnabled())
    }

    fun isCoversEnabled() = displaySettingsInteractor.isCoversEnabled()

    fun getSearchText() = searchText

    private fun addCompositionsToPlayNext(compositions: List<Composition>) {
        playerInteractor.addCompositionsToPlayNext(compositions)
            .subscribeOnUi(viewState::onCompositionsAddedToPlayNext, this::onDefaultError)
    }

    private fun addCompositionsToEnd(compositions: List<Composition>) {
        playerInteractor.addCompositionsToEnd(compositions)
            .subscribeOnUi(viewState::onCompositionsAddedToQueue, this::onDefaultError)
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun deleteItem(playListItem: PlayListItem) {
        playListsInteractor.deleteItemFromPlayList(playListItem, playListId)
            .subscribeOnUi({ onDeleteItemCompleted(playListItem) }, this::onDeleteItemError)
    }

    private fun swapItems(from: Int, to: Int) {
        if (!ListUtils.isIndexInRange(items, from) || !ListUtils.isIndexInRange(items, to)) {
            return
        }

        Collections.swap(items, from, to)
        viewState.notifyItemMoved(from, to)
    }

    private fun onPlayListDeletingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeletePlayListError(errorCommand)
    }

    private fun onPlayListDeleted(playList: PlayList) {
        viewState.showPlayListDeleteSuccess(playList)
    }

    private fun onDeleteItemCompleted(item: PlayListItem) {
        playList.call { playList -> viewState.showDeleteItemCompleted(playList, listOf(item)) }
    }

    private fun onDeleteItemError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteItemError(errorCommand)
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

    private fun deletePreparedCompositions() {
        lastDeleteAction = playerInteractor.deleteCompositions(compositionsToDelete)
            .observeOn(uiScheduler)
            .doOnComplete(this::onDeleteCompositionsSuccess)

        lastDeleteAction!!.justSubscribe(this::onDeleteCompositionsError)
    }

    private fun onDeleteCompositionsSuccess() {
        viewState.showDeletedCompositionMessage(compositionsToDelete)
    }

    private fun onDeleteCompositionsError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun subscribeOnCompositions() {
        if (items.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(itemsDisposable)
        itemsDisposable = playListsInteractor.getCompositionsObservable(playListId, searchText)
            .observeOn(uiScheduler)
            .filter(listDragFilter::filterListEmitting)
            .subscribe(
                this::onPlayListsReceived,
                { viewState.closeScreen() },
                viewState::closeScreen,
                presenterDisposable
            )
    }

    private fun subscribePlayList() {
        playListsInteractor.getPlayListObservable(playListId)
            .subscribeOnUi(
                this::onPlayListInfoReceived,
                { viewState.closeScreen() },
                viewState::closeScreen
            )
    }

    private fun onPlayListInfoReceived(playList: PlayList) {
        this.playList.setObject(playList)
        viewState.showPlayListInfo(playList)
    }

    private fun onPlayListsReceived(list: List<PlayListItem>) {
        val firstReceive = this.items.isEmpty()

        items = list
        viewState.updateItemsList(list)
        if (items.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                viewState.showEmptyList()
            } else {
                viewState.showEmptySearchResult()
            }
        } else {
            viewState.showList()
            if (firstReceive) {
                val listPosition = playListsInteractor.savedListPosition
                if (listPosition != null) {
                    viewState.restoreListPosition(listPosition)
                }
            }

            if (RxUtils.isInactive(currentItemDisposable)) {
                subscribeOnCurrentComposition()
            }
        }
    }

    private fun subscribeOnCurrentComposition() {
        currentItemDisposable = playerInteractor.getCurrentQueueItemObservable()
            .observeOn(uiScheduler)
            .subscribe(this::onCurrentCompositionReceived, errorParser::logError)
        presenterBatterySafeDisposable.add(currentItemDisposable!!)
    }

    private fun onCurrentCompositionReceived(playQueueEvent: PlayQueueEvent) {
        currentItem = playQueueEvent.playQueueItem
    }

    private fun subscribeOnRepeatMode() {
        playerInteractor.getRandomPlayingObservable()
            .subscribeOnUi(viewState::showRandomMode, errorParser::logError)
    }

}