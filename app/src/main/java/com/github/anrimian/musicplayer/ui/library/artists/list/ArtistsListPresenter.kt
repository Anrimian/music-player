package com.github.anrimian.musicplayer.ui.library.artists.list

import com.github.anrimian.musicplayer.data.utils.rx.mapError
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.artist.Artist
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

class ArtistsListPresenter(
    private val interactor: LibraryArtistsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
): BaseLibraryPresenter<ArtistsListView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private var artistsDisposable: Disposable? = null

    private var artists: List<Artist> = ArrayList()
    private val selectedArtists = LinkedHashSet<Artist>()

    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnArtistsList()
    }

    fun onStop(listPosition: ListPosition?) {
        interactor.saveListPosition(listPosition)
    }

    fun onFragmentResumed() {
        interactor.setSelectedArtistScreen(0)
    }

    fun onTryAgainLoadCompositionsClicked() {
        subscribeOnArtistsList()
    }

    fun onArtistClicked(position: Int, artist: Artist) {
        if (selectedArtists.isEmpty()) {
            viewState.goToArtistScreen(artist)
            return
        }
        if (selectedArtists.contains(artist)) {
            selectedArtists.remove(artist)
            viewState.onArtistUnselected(artist, position)
        } else {
            selectedArtists.add(artist)
            viewState.onArtistSelected(artist, position)
        }
        viewState.showSelectionMode(selectedArtists.size)
    }

    fun onArtistLongClicked(position: Int, artist: Artist) {
        selectedArtists.add(artist)
        viewState.showSelectionMode(selectedArtists.size)
        viewState.onArtistSelected(artist, position)
    }

    fun onPlayAllSelectedClicked() {
        playSelectedArtists()
    }

    fun onSelectAllButtonClicked() {
        selectedArtists.clear()
        selectedArtists.addAll(artists)
        viewState.showSelectionMode(artists.size)
        viewState.setItemsSelected(true)
    }

    fun onPlayNextSelectedArtistsClicked() {
        addArtistsToPlayNext(ArrayList(selectedArtists))
        closeSelectionMode()
    }

    fun onAddToQueueSelectedArtistsClicked() {
        addArtistsToPlayQueue(ArrayList(selectedArtists))
        closeSelectionMode()
    }

    fun onAddSelectedArtistsToPlayListClicked() {
        viewState.showSelectPlayListDialog(selectedArtists, true)
    }

    fun onPlayListToAddingSelected(playList: PlayList, artists: LongArray, closeMultiselect: Boolean) {
        performAddToPlaylist(interactor.getAllCompositionsByArtistIds(artists), playList) {
            onAddingToPlayListCompleted(closeMultiselect)
        }
    }

    fun onShareSelectedArtistsClicked() {
        shareArtistCompositions(ArrayList(selectedArtists))
    }

    fun onSelectionModeBackPressed() {
        closeSelectionMode()
    }

    fun onPlayArtistClicked(artist: Artist) {
        startPlaying(listOf(artist))
    }

    fun onPlayNextArtistClicked(position: Int) {
        addArtistsToPlayNext(listOf(artists[position]))
    }

    fun onPlayNextArtistClicked(artist: Artist) {
        addArtistsToPlayNext(listOf(artist))
    }

    fun onAddToQueueArtistClicked(artist: Artist) {
        addArtistsToPlayQueue(listOf(artist))
    }

    fun onAddArtistToPlayListClicked(artist: Artist) {
        viewState.showSelectPlayListDialog(listOf(artist), false)
    }

    fun onShareArtistClicked(artist: Artist) {
        shareArtistCompositions(listOf(artist))
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
            subscribeOnArtistsList()
        }
    }

    fun getSelectedArtists(): HashSet<Artist> = selectedArtists

    fun getSearchText() = searchText

    private fun shareArtistCompositions(artists: List<Artist>) {
        interactor.getAllCompositionsForArtists(artists)
            .mapError(::ReceiveCompositionsForSendException)
            .launchOnUi(viewState::sendCompositions, viewState::showErrorMessage)
    }

    private fun onAddingToPlayListCompleted(closeMultiselect: Boolean) {
        if (closeMultiselect && selectedArtists.isNotEmpty()) {
            closeSelectionMode()
        }
    }

    private fun playSelectedArtists() {
        startPlaying(ArrayList(selectedArtists))
        closeSelectionMode()
    }

    private fun startPlaying(artists: List<Artist>) {
        interactor.startPlaying(artists).runOnUi(viewState::showErrorMessage)
    }

    private fun addArtistsToPlayNext(artists: List<Artist>) {
        addCompositionsToPlayNext(interactor.getAllCompositionsForArtists(artists))
    }

    private fun addArtistsToPlayQueue(artists: List<Artist>) {
        addCompositionsToEndOfQueue(interactor.getAllCompositionsForArtists(artists))
    }

    private fun closeSelectionMode() {
        selectedArtists.clear()
        viewState.showSelectionMode(0)
        viewState.setItemsSelected(false)
    }

    private fun subscribeOnArtistsList() {
        if (artists.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(artistsDisposable, presenterDisposable)
        artistsDisposable = interactor.getArtistsObservable(searchText)
            .observeOn(uiScheduler)
            .subscribe(this::onArtistsReceived, this::onArtistsReceivingError)
        presenterDisposable.add(artistsDisposable!!)
    }

    private fun onArtistsReceivingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showLoadingError(errorCommand)
    }

    private fun onArtistsReceived(artists: List<Artist>) {
        val firstReceive = this.artists.isEmpty()

        this.artists = artists
        viewState.submitList(artists)
        if (artists.isEmpty()) {
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