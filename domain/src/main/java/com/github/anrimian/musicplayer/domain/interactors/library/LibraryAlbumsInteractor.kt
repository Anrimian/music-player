package com.github.anrimian.musicplayer.domain.interactors.library

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class LibraryAlbumsInteractor(
    private val libraryRepository: LibraryRepository,
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlayListsInteractor,
    private val settingsRepository: SettingsRepository,
    private val uiStateRepository: UiStateRepository
) {

    fun getAlbumsObservable(searchText: String?): Observable<List<Album>> {
        return libraryRepository.getAlbumsObservable(searchText)
    }

    fun getAlbumItemsObservable(albumId: Long): Observable<List<AlbumComposition>> {
        return libraryRepository.getAlbumItemsObservable(albumId)
    }

    fun getAlbumObservable(albumId: Long): Observable<Album> {
        return libraryRepository.getAlbumObservable(albumId)
    }

    fun getOrder(): Order = settingsRepository.albumsOrder

    fun setOrder(order: Order) {
        settingsRepository.albumsOrder = order
    }

    fun getSavedListPosition(): ListPosition? = uiStateRepository.savedAlbumsListPosition

    fun saveListPosition(listPosition: ListPosition?) {
        uiStateRepository.saveAlbumsListPosition(listPosition)
    }

    fun getSavedItemsListPosition(albumId: Long): ListPosition? {
        return uiStateRepository.getSavedAlbumListPosition(albumId)
    }

    fun saveItemsListPosition(albumId: Long, listPosition: ListPosition?) {
        uiStateRepository.saveAlbumListPosition(albumId, listPosition)
    }

    fun startPlaying(albums: List<Album>) {
        libraryRepository.getCompositionIdsInAlbums(albums)
            .subscribe(playerInteractor::startPlaying)
    }

    fun addAlbumsToPlayNext(albums: List<Album>): Single<List<Composition>> {
        return libraryRepository.getCompositionsInAlbums(albums)
            .flatMap(playerInteractor::addCompositionsToPlayNext)
    }

    fun addAlbumsToQueue(albums: List<Album>): Single<List<Composition>> {
        return libraryRepository.getCompositionsInAlbums(albums)
            .flatMap(playerInteractor::addCompositionsToEnd)
    }

    fun addAlbumsToPlayList(
        albumIds: LongArray,
        playList: PlayList
    ): Single<List<Composition>> {
        return libraryRepository.getCompositionsByAlbumIds(albumIds.asIterable())
            .flatMap { compositions ->
                playListsInteractor.addCompositionsToPlayList(compositions, playList)
                    .toSingleDefault(compositions)
            }
    }

    fun getCompositionsInAlbums(albums: List<Album>): Single<List<Composition>> {
        return libraryRepository.getCompositionsInAlbums(albums)
    }
}