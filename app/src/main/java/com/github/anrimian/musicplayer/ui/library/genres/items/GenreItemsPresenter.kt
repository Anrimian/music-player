package com.github.anrimian.musicplayer.ui.library.genres.items

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class GenreItemsPresenter(
    private val genreId: Long,
    private val interactor: LibraryGenresInteractor,
    playListsInteractor: PlayListsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    displaySettingsInteractor: DisplaySettingsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryCompositionsPresenter<GenreItemsView>(
    playerInteractor,
    playListsInteractor,
    displaySettingsInteractor,
    errorParser,
    uiScheduler
) {

    private var changeDisposable: Disposable? = null
    private var genre: Genre? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnGenreInfo()
    }

    override fun getCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return interactor.getGenreItemsObservable(genreId)
    }

    override fun getSavedListPosition(): ListPosition? {
        return null
    }

    override fun saveListPosition(listPosition: ListPosition) {}

    fun onFragmentMovedToTop() {
        //save selected genre screen. Wait a little for all screens
    }

    fun onRenameGenreClicked() {
        genre?.let(viewState::showRenameGenreDialog)
    }

    fun onNewGenreNameEntered(name: String?, genreId: Long) {
        RxUtils.dispose(changeDisposable)
        changeDisposable = interactor.updateGenreName(name, genreId)
            .observeOn(uiScheduler)
            .doOnSubscribe { viewState.showRenameProgress() }
            .doFinally { viewState.hideRenameProgress() }
            .subscribe({}, this::onDefaultError)
    }

    private fun subscribeOnGenreInfo() {
        interactor.getGenreObservable(genreId)
            .subscribeOnUi(
                this::onGenreInfoReceived,
                { viewState.closeScreen() },
                viewState::closeScreen
            )

    }

    private fun onGenreInfoReceived(genre: Genre) {
        this.genre = genre
        viewState.showGenreInfo(genre)
    }

}