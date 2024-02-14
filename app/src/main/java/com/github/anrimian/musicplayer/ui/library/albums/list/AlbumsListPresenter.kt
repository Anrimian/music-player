package com.github.anrimian.musicplayer.ui.library.albums.list

import com.github.anrimian.musicplayer.data.utils.rx.mapError
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.share.models.ReceiveCompositionsForSendException
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class AlbumsListPresenter(
    private val interactor: LibraryAlbumsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryPresenter<AlbumsListView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private var albumsDisposable: Disposable? = null

    private var albums: List<Album> = ArrayList()
    private val selectedAlbums = LinkedHashSet<Album>()

    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnAlbumsList()
    }

    fun onFragmentResumed() {
        interactor.setSelectedAlbumScreen(0L)
    }

    fun onStop(listPosition: ListPosition) {
        interactor.saveListPosition(listPosition)
    }

    fun onTryAgainLoadCompositionsClicked() {
        subscribeOnAlbumsList()
    }

    fun onAlbumClicked(position: Int, album: Album) {
        if (selectedAlbums.isEmpty()) {
            viewState.goToAlbumScreen(album)
            return
        }
        if (selectedAlbums.contains(album)) {
            selectedAlbums.remove(album)
            viewState.onAlbumUnselected(album, position)
        } else {
            selectedAlbums.add(album)
            viewState.onAlbumSelected(album, position)
        }
        viewState.showSelectionMode(selectedAlbums.size)
    }

    fun onAlbumLongClicked(position: Int, album: Album) {
        selectedAlbums.add(album)
        viewState.showSelectionMode(selectedAlbums.size)
        viewState.onAlbumSelected(album, position)
    }

    fun onPlayAllSelectedClicked() {
        playSelectedAlbums()
    }

    fun onSelectAllButtonClicked() {
        selectedAlbums.clear()
        selectedAlbums.addAll(albums)
        viewState.showSelectionMode(albums.size)
        viewState.setItemsSelected(true)
    }

    fun onPlayNextSelectedAlbumsClicked() {
        addAlbumsToPlayNext(ArrayList(selectedAlbums))
        closeSelectionMode()
    }

    fun onAddToQueueSelectedAlbumsClicked() {
        addAlbumsToPlayQueue(ArrayList(selectedAlbums))
        closeSelectionMode()
    }

    fun onAddSelectedAlbumsToPlayListClicked() {
        viewState.showSelectPlayListDialog(selectedAlbums, true)
    }

    fun onPlayListToAddingSelected(playList: PlayList, albums: LongArray, closeMultiselect: Boolean) {
        performAddToPlaylist(interactor.getCompositionsByAlbumIds(albums), playList) {
            onAddingToPlayListCompleted(closeMultiselect)
        }
    }

    fun onShareSelectedAlbumsClicked() {
        shareAlbumsCompositions(ArrayList(selectedAlbums))
    }

    fun onSelectionModeBackPressed() {
        closeSelectionMode()
    }

    fun onPlayAlbumClicked(album: Album) {
        startPlaying(listOf(album))
    }

    fun onPlayNextAlbumClicked(position: Int) {
        addAlbumsToPlayNext(listOf(albums[position]))
    }

    fun onPlayNextAlbumClicked(album: Album) {
        addAlbumsToPlayNext(listOf(album))
    }

    fun onAddToQueueAlbumClicked(album: Album) {
        addAlbumsToPlayQueue(listOf(album))
    }

    fun onAddAlbumToPlayListClicked(album: Album) {
        viewState.showSelectPlayListDialog(listOf(album), false)
    }

    fun onShareAlbumClicked(album: Album) {
        shareAlbumsCompositions(listOf(album))
    }

    fun onOrderMenuItemClicked() {
        viewState.showSelectOrderScreen(interactor.getOrder())
    }

    fun onOrderSelected(order: Order) {
        interactor.setOrder(order)
    }

    fun onSearchTextChanged(text: String?) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text
            subscribeOnAlbumsList()
        }
    }

    fun getSelectedAlbums(): HashSet<Album> = selectedAlbums

    fun getSearchText() = searchText

    private fun shareAlbumsCompositions(albums: List<Album>) {
        interactor.getCompositionsInAlbums(albums)
            .mapError(::ReceiveCompositionsForSendException)
            .launchOnUi(viewState::sendCompositions, viewState::showErrorMessage)
    }

    private fun onAddingToPlayListCompleted(closeMultiselect: Boolean) {
        if (closeMultiselect && selectedAlbums.isNotEmpty()) {
            closeSelectionMode()
        }
    }

    private fun playSelectedAlbums() {
        startPlaying(ArrayList(selectedAlbums))
        closeSelectionMode()
    }

    private fun startPlaying(albums: List<Album>) {
        interactor.startPlaying(albums).runOnUi(viewState::showErrorMessage)
    }

    private fun addAlbumsToPlayNext(albums: List<Album>) {
        addCompositionsToPlayNext(interactor.getCompositionsInAlbums(albums))
    }

    private fun addAlbumsToPlayQueue(albums: List<Album>) {
        addCompositionsToEndOfQueue(interactor.getCompositionsInAlbums(albums))
    }

    private fun closeSelectionMode() {
        selectedAlbums.clear()
        viewState.showSelectionMode(0)
        viewState.setItemsSelected(false)
    }

    private fun subscribeOnAlbumsList() {
        if (albums.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(albumsDisposable, presenterDisposable)
        albumsDisposable = interactor.getAlbumsObservable(searchText)
            .observeOn(uiScheduler)
            .subscribe(this::onAlbumsReceived, this::onAlbumsReceivingError)
        presenterDisposable.add(albumsDisposable!!)
    }

    private fun onAlbumsReceivingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showLoadingError(errorCommand)
    }

    private fun onAlbumsReceived(albums: List<Album>) {
        val firstReceive = this.albums.isEmpty()

        this.albums = albums
        viewState.submitList(albums)
        if (albums.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                viewState.showEmptyList()
            } else {
                viewState.showEmptySearchResult()
            }
        } else {
            viewState.showList()
            if (firstReceive) {
                val listPosition = interactor.getSavedListPosition()
                if (listPosition != null) {
                    viewState.restoreListPosition(listPosition)
                }
            }
        }
    }

}