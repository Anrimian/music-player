package com.github.anrimian.musicplayer.ui.library.albums.items

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler

class AlbumItemsPresenter(
    private val albumId: Long,
    private val interactor: LibraryAlbumsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    displaySettingsInteractor: DisplaySettingsInteractor,
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryCompositionsPresenter<AlbumComposition, AlbumItemsView>(
    displaySettingsInteractor,
    syncInteractor,
    playerInteractor,
    playListsInteractor,
    errorParser,
    uiScheduler
) {

    private var album: Album? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnAlbumInfo()
    }

    override fun getCompositionsObservable(searchText: String?): Observable<List<AlbumComposition>> {
        return interactor.getAlbumItemsObservable(albumId)
    }

    override fun getSavedListPosition(): ListPosition? {
        return interactor.getSavedItemsListPosition(albumId)
    }

    override fun saveListPosition(listPosition: ListPosition) {
        interactor.saveItemsListPosition(albumId, listPosition)
    }

    fun onFragmentResumed() {
        interactor.setSelectedAlbumScreen(albumId)
    }

    fun onEditAlbumClicked() {
        album?.let(viewState::showEditAlbumScreen)
    }

    private fun subscribeOnAlbumInfo() {
        interactor.getAlbumObservable(albumId)
            .subscribeOnUi(
                this::onAlbumInfoReceived,
                { viewState.closeScreen() },
                viewState::closeScreen
            )
    }

    private fun onAlbumInfoReceived(album: Album) {
        this.album = album
        viewState.showAlbumInfo(album)
    }

}