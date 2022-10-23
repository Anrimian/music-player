package com.github.anrimian.musicplayer.ui.library.genres.list

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class GenresListPresenter(private val interactor: LibraryGenresInteractor,
                          errorParser: ErrorParser,
                          uiScheduler: Scheduler
) : AppPresenter<GenresListView>(uiScheduler, errorParser) {
  
    private var listDisposable: Disposable? = null
    private var changeDisposable: Disposable? = null
    
    private var genres: List<Genre> = ArrayList()
    
    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnGenresList()
    }

    fun onTryAgainLoadCompositionsClicked() {
        subscribeOnGenresList()
    }

    fun onOrderMenuItemClicked() {
        viewState.showSelectOrderScreen(interactor.order)
    }

    fun onOrderSelected(order: Order) {
        interactor.order = order
    }

    fun onNewGenreNameEntered(name: String, genreId: Long) {
        RxUtils.dispose(changeDisposable)
        changeDisposable = interactor.updateGenreName(name, genreId)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showRenameProgress() }
                .doFinally { viewState.hideRenameProgress() }
                .subscribe({}, this::onDefaultError)
    }

    fun onSearchTextChanged(text: String?) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text
            subscribeOnGenresList()
        }
    }

    fun getSearchText() = searchText

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
        }
    }

    private fun onDefaultError(throwable: Throwable) {
        viewState.showErrorMessage(errorParser.parseError(throwable))
    }
}