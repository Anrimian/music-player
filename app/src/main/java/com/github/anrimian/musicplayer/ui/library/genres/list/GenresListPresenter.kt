package com.github.anrimian.musicplayer.ui.library.genres.list

import com.github.anrimian.musicplayer.data.utils.rx.mapError
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.genres.Genre
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

class GenresListPresenter(
    private val interactor: LibraryGenresInteractor,
    playerInteractor: LibraryPlayerInteractor,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryPresenter<GenresListView>(
    playerInteractor,
    playListsInteractor,
    uiScheduler,
    errorParser
) {

    private var listDisposable: Disposable? = null

    private var genres: List<Genre> = ArrayList()
    private val selectedGenres = LinkedHashSet<Genre>()

    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnGenresList()
    }

    fun onFragmentResumed() {
        interactor.setSelectedGenreScreen(0L)
    }

    fun onStop(listPosition: ListPosition) {
        interactor.saveListPosition(listPosition)
    }
    
    fun onTryAgainLoadCompositionsClicked() {
        subscribeOnGenresList()
    }

    fun onGenreClicked(position: Int, genre: Genre) {
        if (selectedGenres.isEmpty()) {
            viewState.goToGenreScreen(genre)
            return
        }
        if (selectedGenres.contains(genre)) {
            selectedGenres.remove(genre)
            viewState.onGenreUnselected(genre, position)
        } else {
            selectedGenres.add(genre)
            viewState.onGenreSelected(genre, position)
        }
        viewState.showSelectionMode(selectedGenres.size)
    }

    fun onGenreLongClicked(position: Int, genre: Genre) {
        selectedGenres.add(genre)
        viewState.showSelectionMode(selectedGenres.size)
        viewState.onGenreSelected(genre, position)
    }

    fun onPlayAllSelectedClicked() {
        playSelectedGenres()
    }

    fun onSelectAllButtonClicked() {
        selectedGenres.clear()
        selectedGenres.addAll(genres)
        viewState.showSelectionMode(genres.size)
        viewState.setItemsSelected(true)
    }
    
    fun onPlayNextSelectedGenresClicked() {
        addGenresToPlayNext(ArrayList(selectedGenres))
        closeSelectionMode()
    }

    fun onAddToQueueSelectedGenresClicked() {
        addGenresToPlayQueue(ArrayList(selectedGenres))
        closeSelectionMode()
    }

    fun onAddSelectedGenresToPlayListClicked() {
        viewState.showSelectPlayListDialog(selectedGenres, true)
    }

    fun onPlayListToAddingSelected(playList: PlayList, genres: LongArray, closeMultiselect: Boolean) {
        performAddToPlaylist(interactor.getCompositionsInGenresIds(genres), playList) {
            onAddingToPlayListCompleted(closeMultiselect)
        }
    }

    fun onShareSelectedGenresClicked() {
        shareGenresCompositions(ArrayList(selectedGenres))
    }

    fun onSelectionModeBackPressed() {
        closeSelectionMode()
    }

    fun onPlayGenreClicked(genre: Genre) {
        startPlaying(listOf(genre))
    }

    fun onPlayNextGenreClicked(position: Int) {
        addGenresToPlayNext(listOf(genres[position]))
    }

    fun onPlayNextGenreClicked(genre: Genre) {
        addGenresToPlayNext(listOf(genre))
    }

    fun onAddToQueueGenreClicked(genre: Genre) {
        addGenresToPlayQueue(listOf(genre))
    }

    fun onAddGenreToPlayListClicked(genre: Genre) {
        viewState.showSelectPlayListDialog(listOf(genre), false)
    }

    fun onShareGenreClicked(genre: Genre) {
        shareGenresCompositions(listOf(genre))
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
            subscribeOnGenresList()
        }
    }

    fun getSelectedGenres(): HashSet<Genre> = selectedGenres

    fun getSearchText() = searchText

    private fun shareGenresCompositions(genres: List<Genre>) {
        interactor.getCompositionsInGenres(genres)
            .mapError(::ReceiveCompositionsForSendException)
            .launchOnUi(viewState::sendCompositions, viewState::showErrorMessage)
    }

    private fun onAddingToPlayListCompleted(closeMultiselect: Boolean) {
        if (closeMultiselect && selectedGenres.isNotEmpty()) {
            closeSelectionMode()
        }
    }

    private fun playSelectedGenres() {
        startPlaying(ArrayList(selectedGenres))
        closeSelectionMode()
    }

    private fun startPlaying(genres: List<Genre>) {
        interactor.startPlaying(genres).runOnUi(viewState::showErrorMessage)
    }

    private fun addGenresToPlayNext(genres: List<Genre>) {
        addCompositionsToPlayNext(interactor.getCompositionsInGenres(genres))
    }

    private fun addGenresToPlayQueue(genres: List<Genre>) {
        addCompositionsToEndOfQueue(interactor.getCompositionsInGenres(genres))
    }

    private fun closeSelectionMode() {
        selectedGenres.clear()
        viewState.showSelectionMode(0)
        viewState.setItemsSelected(false)
    }

    private fun subscribeOnGenresList() {
        if (genres.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(listDisposable, presenterDisposable)
        listDisposable = interactor.getGenresObservable(searchText)
            .observeOn(uiScheduler)
            .subscribe(this::onGenresReceived, this::onGenresReceivingError)
        presenterDisposable.add(listDisposable!!)
    }

    private fun onGenresReceivingError(throwable: Throwable) {
        viewState.showLoadingError(errorParser.parseError(throwable))
    }

    private fun onGenresReceived(genres: List<Genre>) {
        val firstReceive = this.genres.isEmpty()

        this.genres = genres
        viewState.submitList(genres)
        if (genres.isEmpty()) {
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