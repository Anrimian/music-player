package com.github.anrimian.musicplayer.ui.playlist_screens.playlist

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils
import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.utils.model.Item
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import java.util.*

@InjectViewState
class PlayListPresenter(private val playListId: Long,
                        private val playerInteractor: MusicPlayerInteractor,
                        private val playListsInteractor: PlayListsInteractor,
                        private val displaySettingsInteractor: DisplaySettingsInteractor,
                        errorParser: ErrorParser,
                        uiScheduler: Scheduler)
    : AppPresenter<PlayListView>(uiScheduler, errorParser) {

    private val presenterBatterySafeDisposable = CompositeDisposable()

    private var currentItemDisposable: Disposable? = null
    private var items: List<PlayListItem> = ArrayList()
    private var playList: PlayList? = null

    private val compositionsForPlayList: MutableList<Composition> = LinkedList()
    private val compositionsToDelete: MutableList<Composition> = LinkedList()

    private var startDragPosition = 0
    private var currentItem: PlayQueueItem? = null
    private var deletedItem: Item<PlayListItem>? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnCompositions()
        subscribePlayList()
        subscribeOnCurrentComposition()
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

    fun onStop() {
        presenterBatterySafeDisposable.clear()
    }

    fun onCompositionClicked(playListItem: PlayListItem?, position: Int) {
        viewState.showCompositionActionDialog(playListItem, position)
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

    fun onDeleteFromPlayListButtonClicked(playListItem: PlayListItem, position: Int) {
        deleteItem(playListItem, position)
    }

    fun onDeletePlayListButtonClicked() {
        viewState.showConfirmDeletePlayListDialog(playList)
    }

    fun onDeletePlayListDialogConfirmed() {
        playListsInteractor.deletePlayList(playList!!.id)
                .subscribeOnUi(this::onPlayListDeleted, this::onPlayListDeletingError)
    }

    fun onFragmentMovedToTop() {
        playListsInteractor.setSelectedPlayListScreen(playListId)
    }

    fun onItemSwipedToDelete(position: Int) {
        deleteItem(items[position], position)
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
        playListsInteractor.moveItemInPlayList(playList, startDragPosition, position)
    }

    fun onPlayActionSelected(position: Int) {
        playerInteractor.startPlaying(items.map(PlayListItem::getComposition), position)
    }

    fun onRestoreRemovedItemClicked() {
        val removedComposition = deletedItem!!.data!!.composition
        playListsInteractor.addCompositionsToPlayList(listOf(removedComposition),
                        playList,
                        deletedItem!!.position)
                .justSubscribe(this::onDefaultError)
    }

    fun onChangePlayListNameButtonClicked() {
        if (playList != null) {
            viewState.showEditPlayListNameDialog(playList)
        }
    }

    fun isCoversEnabled() = displaySettingsInteractor.isCoversEnabled

    private fun addCompositionsToPlayNext(compositions: List<Composition>) {
        playerInteractor.addCompositionsToPlayNext(compositions)
                .subscribeOnUi(viewState::onCompositionsAddedToPlayNext, this::onDefaultError)
    }

    private fun addCompositionsToEnd(compositions: List<Composition>) {
        playerInteractor.addCompositionsToEnd(compositions)
                .subscribeOnUi(viewState::onCompositionsAddedToPlayNext, this::onDefaultError)
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun deleteItem(playListItem: PlayListItem, position: Int) {
        playListsInteractor.deleteItemFromPlayList(playListItem, playListId)
                .subscribeOnUi({ onDeleteItemCompleted(playListItem, position) }, this::onDeleteItemError)
    }

    private fun swapItems(from: Int, to: Int) {
        Collections.swap(items, from, to)
        viewState.notifyItemMoved(from, to)
    }

    private fun onPlayListDeletingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeletePlayListError(errorCommand)
    }

    private fun onPlayListDeleted() {
        viewState.showPlayListDeleteSuccess(playList)
    }

    private fun onDeleteItemCompleted(item: PlayListItem, position: Int) {
        if (playList != null) {
            deletedItem = Item(item, position)
            viewState.showDeleteItemCompleted(playList, listOf(item))
        }
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
        playerInteractor.deleteCompositions(compositionsToDelete)
                .subscribeOnUi(this::onDeleteCompositionsSuccess, this::onDeleteCompositionsError)
    }

    private fun onDeleteCompositionsSuccess() {
        viewState.showDeletedCompositionMessage(compositionsToDelete)
    }

    private fun onDeleteCompositionsError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun subscribeOnCompositions() {
        viewState.showLoading()
        playListsInteractor.getCompositionsObservable(playListId)
                .subscribeOnUi(
                        this::onPlayListsReceived,
                        { viewState.closeScreen() },
                        viewState::closeScreen
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
        this.playList = playList
        viewState.showPlayListInfo(playList)
    }

    private fun onPlayListsReceived(list: List<PlayListItem>) {
        items = list
        viewState.updateItemsList(list)
        if (items.isEmpty()) {
            viewState.showEmptyList()
        } else {
            viewState.showList()
            if (RxUtils.isInactive(currentItemDisposable)) {
                subscribeOnCurrentComposition()
            }
        }
    }

    private fun subscribeOnCurrentComposition() {
        currentItemDisposable = playerInteractor.currentQueueItemObservable
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionReceived, errorParser::logError)
        presenterBatterySafeDisposable.add(currentItemDisposable!!)
    }

    private fun onCurrentCompositionReceived(playQueueEvent: PlayQueueEvent) {
        currentItem = playQueueEvent.playQueueItem
    }

}