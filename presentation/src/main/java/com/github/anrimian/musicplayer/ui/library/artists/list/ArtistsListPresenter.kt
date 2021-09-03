package com.github.anrimian.musicplayer.ui.library.artists.list

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

class ArtistsListPresenter(private val interactor: LibraryArtistsInteractor,
                           errorParser: ErrorParser,
                           uiScheduler: Scheduler) 
    : AppPresenter<ArtistsListView>(uiScheduler, errorParser) {
    
    private var artistsDisposable: Disposable? = null

    private var artists: List<Artist> = ArrayList()
    
    private var searchText: String? = null

    private var lastEditAction: Completable? = null

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

    fun onNewArtistNameEntered(name: String?, artistId: Long) {
        lastEditAction = interactor.updateArtistName(name, artistId)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showRenameProgress() }
                .doFinally { viewState.hideRenameProgress() }
        lastEditAction!!.justSubscribe(this::onDefaultError)
    }

    fun onRetryFailedEditActionClicked() {
        if (lastEditAction != null) {
            lastEditAction!!.doFinally { lastEditAction = null }.justSubscribe(this::onDefaultError)
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

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

}