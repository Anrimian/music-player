package com.github.anrimian.musicplayer.ui.library.artists.items

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler

class ArtistItemsPresenter(
    private val artistId: Long,
    private val interactor: LibraryArtistsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    displaySettingsInteractor: DisplaySettingsInteractor,
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryCompositionsPresenter<Composition, ArtistItemsView>(
    displaySettingsInteractor,
    syncInteractor,
    playerInteractor,
    playListsInteractor,
    errorParser,
    uiScheduler
) {

    private var artist: Artist? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnArtistInfo()
        subscribeOnArtistAlbums()
    }

    override fun getCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return interactor.getCompositionsByArtist(artistId)
    }

    override fun getSavedListPosition(): ListPosition? {
        return interactor.getSavedItemsListPosition(artistId)
    }

    override fun saveListPosition(listPosition: ListPosition) {
        interactor.saveItemsListPosition(artistId, listPosition)
    }

    fun onFragmentResumed() {
        interactor.setSelectedArtistScreen(artistId)
        if (artist != null) {
            viewState.showArtistInfo(artist!!)
        }
    }

    fun onRenameArtistClicked() {
        if (artist != null) {
            viewState.showRenameArtistDialog(artist!!)
        }
    }

    private fun subscribeOnArtistInfo() {
        interactor.getArtistObservable(artistId).subscribeOnUi(
            this::onArtistInfoReceived,
            { viewState.closeScreen() },
            viewState::closeScreen
        )
    }

    private fun onArtistInfoReceived(artist: Artist) {
        this.artist = artist
        viewState.showArtistInfo(artist)
    }

    private fun subscribeOnArtistAlbums() {
        interactor.getAllAlbumsForArtist(artistId).subscribeOnUi(
            viewState::showArtistAlbums,
            { viewState.closeScreen() },
            viewState::closeScreen
        )
    }
}