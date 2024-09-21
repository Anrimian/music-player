package com.github.anrimian.musicplayer.ui.library.genres.items

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler

class GenreItemsPresenter(
    private val genreId: Long,
    private val interactor: LibraryGenresInteractor,
    playerInteractor: LibraryPlayerInteractor,
    displaySettingsInteractor: DisplaySettingsInteractor,
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryCompositionsPresenter<Composition, GenreItemsView>(
    displaySettingsInteractor,
    syncInteractor,
    playerInteractor,
    playListsInteractor,
    errorParser,
    uiScheduler
) {

    private var genre: Genre? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnGenreInfo()
    }

    override fun getCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return interactor.getGenreItemsObservable(genreId)
    }

    override fun getSavedListPosition(): ListPosition? {
        return interactor.getSavedItemsListPosition(genreId)
    }

    override fun saveListPosition(listPosition: ListPosition) {
        interactor.saveItemsListPosition(genreId, listPosition)
    }

    fun onFragmentResumed() {
        interactor.setSelectedGenreScreen(genreId)
    }

    fun onRenameGenreClicked() {
        genre?.let(viewState::showRenameGenreDialog)
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