package com.github.anrimian.musicplayer.ui.library.artists.list

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class ArtistsListPresenter(
    private val interactor: LibraryArtistsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
): AppPresenter<ArtistsListView>(uiScheduler, errorParser) {

    private var artistsDisposable: Disposable? = null

    private var artists: List<Artist> = ArrayList()

    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnArtistsList()
    }

    fun onStop(listPosition: ListPosition) {
        interactor.saveListPosition(listPosition)
    }

    fun onTryAgainLoadCompositionsClicked() {
        subscribeOnArtistsList()
    }

    fun onOrderMenuItemClicked() {
        viewState.showSelectOrderScreen(interactor.order)
    }

    fun onOrderSelected(order: Order?) {
        interactor.order = order
    }

    fun onSearchTextChanged(text: String?) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text
            subscribeOnArtistsList()
        }
    }

    fun getSearchText() = searchText

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
                val listPosition = interactor.savedListPosition
                if (listPosition != null) {
                    viewState.restoreListPosition(listPosition)
                }
            }
        }
    }

}