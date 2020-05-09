package com.github.anrimian.musicplayer.ui.library.artists.list

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.*

class ArtistsListPresenter(private val interactor: LibraryArtistsInteractor,
                           errorParser: ErrorParser,
                           uiScheduler: Scheduler) 
    : AppPresenter<ArtistsListView>(uiScheduler, errorParser) {
    
    private var artistsDisposable: Disposable? = null

    private var artists: List<Artist> = ArrayList()
    
    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnArtistsList()
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

    fun onNewArtistNameEntered(name: String?, artistId: Long) {
        interactor.updateArtistName(name, artistId)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showRenameProgress() }
                .doFinally { viewState.hideRenameProgress() }
                .justSubscribe(this::onDefaultError)
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
        }
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

}