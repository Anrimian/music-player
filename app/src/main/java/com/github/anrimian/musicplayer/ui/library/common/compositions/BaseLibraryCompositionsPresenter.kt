package com.github.anrimian.musicplayer.ui.library.common.compositions

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import java.util.LinkedList

abstract class BaseLibraryCompositionsPresenter<C : Composition, V : BaseLibraryCompositionsView<C>>(
    private val displaySettingsInteractor: DisplaySettingsInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    private val playerInteractor: LibraryPlayerInteractor,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler,
) : BaseLibraryPresenter<V>(playerInteractor, playListsInteractor, uiScheduler, errorParser) {

    private var currentCompositionDisposable: Disposable? = null
    private var compositionsDisposable: Disposable? = null

    private var compositions: List<C> = ArrayList()
    private val selectedCompositions = LinkedHashSet<C>()
    private val compositionsForPlayList: MutableList<Composition> = LinkedList()
    private val compositionsToDelete: MutableList<Composition> = LinkedList()

    private var currentComposition: Composition? = null

    //we can replace it with subj
    private var searchText: String? = null

    private var lastDeleteAction: Completable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnUiSettings()
        subscribeOnRepeatMode()
        subscribeOnCompositions()
        subscribeOnCurrentComposition()
        syncInteractor.getFilesSyncStateObservable()
            .unsafeSubscribeOnUi(viewState::showFilesSyncState)
    }

    fun onStop(listPosition: ListPosition) {
        saveListPosition(listPosition)
    }

    fun onTryAgainLoadCompositionsClicked() {
        subscribeOnCompositions()
    }

    fun onCompositionClicked(position: Int, composition: C) {
        if (selectedCompositions.isEmpty()) {
            if (composition == currentComposition) {
                playerInteractor.playOrPause()
            } else {
                startPlaying(compositions, position)
                viewState.showCurrentComposition(CurrentComposition(composition, true))
            }
            return
        }
        if (selectedCompositions.contains(composition)) {
            selectedCompositions.remove(composition)
            viewState.onCompositionUnselected(composition, position)
        } else {
            selectedCompositions.add(composition)
            viewState.onCompositionSelected(composition, position)
        }
        viewState.showSelectionMode(selectedCompositions.size)
    }

    fun onCompositionIconClicked(position: Int, composition: C) {
        if (composition == currentComposition) {
            playerInteractor.playOrPause()
        } else {
            startPlaying(compositions, position)
            viewState.showCurrentComposition(CurrentComposition(composition, true))
        }
    }

    fun onCompositionLongClick(position: Int, composition: C) {
        selectedCompositions.add(composition)
        viewState.showSelectionMode(selectedCompositions.size)
        viewState.onCompositionSelected(composition, position)
    }

    fun onPlayAllButtonClicked() {
        if (selectedCompositions.isEmpty()) {
            startPlaying(compositions)
        } else {
            playSelectedCompositions()
        }
    }

    fun onDeleteCompositionButtonClicked(composition: Composition) {
        compositionsToDelete.clear()
        compositionsToDelete.add(composition)
        viewState.showConfirmDeleteDialog(compositionsToDelete)
    }

    fun onDeleteSelectedCompositionButtonClicked() {
        compositionsToDelete.clear()
        compositionsToDelete.addAll(selectedCompositions)
        viewState.showConfirmDeleteDialog(compositionsToDelete)
    }

    fun onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions()
    }

    fun onPlayNextCompositionClicked(position: Int) {
        val composition = compositions.elementAtOrNull(position)
        if (composition != null) {
            onPlayNextCompositionClicked(composition)
        }
    }

    fun onPlayNextCompositionClicked(composition: Composition) {
        addCompositionsToPlayNext(ListUtils.asList(composition))
    }

    fun onAddToQueueCompositionClicked(composition: Composition) {
        addCompositionsToEndOfQueue(ListUtils.asList(composition))
    }

    fun onAddToPlayListButtonClicked(composition: Composition) {
        compositionsForPlayList.clear()
        compositionsForPlayList.add(composition)
        viewState.showSelectPlayListDialog()
    }

    fun onAddSelectedCompositionToPlayListClicked() {
        compositionsForPlayList.clear()
        compositionsForPlayList.addAll(selectedCompositions)
        viewState.showSelectPlayListDialog()
    }

    fun onPlayListToAddingSelected(playList: PlayList) {
        performAddToPlaylist(compositionsForPlayList, playList, ::onAddingToPlayListCompleted)
    }

    fun onSelectionModeBackPressed() {
        closeSelectionMode()
    }

    fun onShareSelectedCompositionsClicked() {
        viewState.shareCompositions(selectedCompositions)
    }

    fun onPlayAllSelectedClicked() {
        playSelectedCompositions()
    }

    fun onSelectAllButtonClicked() {
        selectedCompositions.clear() //reselect previous feature
        selectedCompositions.addAll(compositions)
        viewState.showSelectionMode(compositions.size)
        viewState.setItemsSelected(true)
    }

    fun onPlayNextSelectedCompositionsClicked() {
        addCompositionsToPlayNext(ArrayList(selectedCompositions))
        closeSelectionMode()
    }

    fun onAddToQueueSelectedCompositionsClicked() {
        addCompositionsToEndOfQueue(ArrayList(selectedCompositions))
        closeSelectionMode()
    }

    fun onPlayActionSelected(position: Int) {
        startPlaying(compositions, position)
    }

    fun onSearchTextChanged(text: String?) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text
            subscribeOnCompositions()
        }
    }

    fun onRetryFailedDeleteActionClicked() {
        if (lastDeleteAction != null) {
            lastDeleteAction!!
                .doFinally { lastDeleteAction = null }
                .justSubscribe(this::onDeleteCompositionError)
        }
    }

    fun onChangeRandomModePressed() {
        playerInteractor.changeRandomMode()
    }

    fun getSelectedCompositions(): HashSet<C> = selectedCompositions

    fun getSearchText() = searchText

    protected fun subscribeOnCompositions() {
        if (compositions.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(compositionsDisposable, presenterDisposable)
        compositionsDisposable = getCompositionsObservable(searchText)
            .observeOn(uiScheduler)
            .subscribe(this::onCompositionsReceived, this::onCompositionsReceivingError)
        presenterDisposable.add(compositionsDisposable!!)
    }

    protected fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun onDeleteCompositionError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun onAddingToPlayListCompleted() {
        compositionsForPlayList.clear()
        if (selectedCompositions.isNotEmpty()) {
            closeSelectionMode()
        }
    }

    private fun playSelectedCompositions() {
        startPlaying(ArrayList(selectedCompositions))
        closeSelectionMode()
    }

    private fun startPlaying(compositions: List<Composition>, firstPosition: Int = Constants.NO_POSITION) {
        playerInteractor.setCompositionsQueueAndPlay(compositions, firstPosition)
            .runOnUi(viewState::showErrorMessage)
    }

    private fun closeSelectionMode() {
        selectedCompositions.clear()
        viewState.showSelectionMode(0)
        viewState.setItemsSelected(false)
    }

    private fun deletePreparedCompositions() {
        lastDeleteAction = playerInteractor.deleteCompositions(compositionsToDelete)
            .observeOn(uiScheduler)
            .doOnSuccess(this::onDeleteCompositionsSuccess)
            .ignoreElement()
        lastDeleteAction!!.justSubscribe(this::onDeleteCompositionError)
    }

    private fun onDeleteCompositionsSuccess(compositions: List<DeletedComposition>) {
        viewState.showDeleteCompositionMessage(compositions)
        compositionsToDelete.clear()
        if (selectedCompositions.isNotEmpty()) {
            closeSelectionMode()
        }
    }

    private fun subscribeOnCurrentComposition() {
        playerInteractor.getCurrentCompositionObservable()
            .runOnUi(this::onCurrentCompositionReceived, viewState::showErrorMessage)
    }

    private fun onCompositionsReceivingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showLoadingError(errorCommand)
    }

    private fun onCompositionsReceived(compositions: List<C>) {
        val firstReceive = this.compositions.isEmpty()

        this.compositions = compositions
        viewState.updateList(compositions)
        if (compositions.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                viewState.showEmptyList()
            } else {
                viewState.showEmptySearchResult()
            }
        } else {
            viewState.showList()
            if (firstReceive) {
                val listPosition = getSavedListPosition()
                if (listPosition != null) {
                    viewState.restoreListPosition(listPosition)
                }
            }

            if (RxUtils.isInactive(currentCompositionDisposable)) {
                subscribeOnCurrentComposition()
            }
        }
    }

    private fun onCurrentCompositionReceived(currentComposition: CurrentComposition) {
        this.currentComposition = currentComposition.composition
        viewState.showCurrentComposition(currentComposition)
    }

    private fun subscribeOnUiSettings() {
        displaySettingsInteractor.getCoversEnabledObservable()
            .subscribeOnUi(viewState::setDisplayCoversEnabled, errorParser::logError)
    }

    private fun subscribeOnRepeatMode() {
        playerInteractor.getRandomPlayingObservable()
            .subscribeOnUi(viewState::showRandomMode, errorParser::logError)
    }

    protected abstract fun getCompositionsObservable(searchText: String?): Observable<List<C>>
    protected abstract fun getSavedListPosition(): ListPosition?
    protected abstract fun saveListPosition(listPosition: ListPosition)

}