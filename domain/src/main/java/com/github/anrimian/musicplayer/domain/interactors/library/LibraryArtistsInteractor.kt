package com.github.anrimian.musicplayer.domain.interactors.library

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class LibraryArtistsInteractor(
    private val libraryRepository: LibraryRepository,
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlayListsInteractor,
    private val settingsRepository: SettingsRepository,
    private val uiStateRepository: UiStateRepository,
) {

    fun getArtistsObservable(searchText: String?): Observable<List<Artist>> {
        return libraryRepository.getArtistsObservable(searchText)
    }

    fun getCompositionsByArtist(artistId: Long): Observable<List<Composition>> {
        return libraryRepository.getCompositionsByArtist(artistId)
    }

    fun getArtistObservable(artistId: Long): Observable<Artist> {
        return libraryRepository.getArtistObservable(artistId)
    }

    fun getAllAlbumsForArtist(artistId: Long): Observable<List<Album>> {
        return libraryRepository.getAllAlbumsForArtist(artistId)
    }

    fun getOrder(): Order = settingsRepository.artistsOrder

    fun setOrder(order: Order) {
        settingsRepository.artistsOrder = order
    }

    fun getSavedListPosition(): ListPosition? = uiStateRepository.savedArtistsListPosition

    fun saveListPosition(listPosition: ListPosition?) {
        uiStateRepository.saveArtistsListPosition(listPosition)
    }

    fun getSavedItemsListPosition(artistId: Long): ListPosition? {
        return uiStateRepository.getSavedArtistListPosition(artistId)
    }

    fun saveItemsListPosition(artistId: Long, listPosition: ListPosition?) {
        uiStateRepository.saveArtistListPosition(artistId, listPosition)
    }

    fun startPlaying(artists: List<Artist>) {
        libraryRepository.getAllCompositionIdsByArtists(artists)
            .subscribe(playerInteractor::startPlaying)
    }

    fun addArtistsToPlayNext(artists: List<Artist>): Single<List<Composition>> {
        return libraryRepository.getAllCompositionsByArtists(artists)
            .flatMap(playerInteractor::addCompositionsToPlayNext)
    }

    fun addArtistsToQueue(artists: List<Artist>): Single<List<Composition>> {
        return libraryRepository.getAllCompositionsByArtists(artists)
            .flatMap(playerInteractor::addCompositionsToEnd)
    }

    fun addArtistsToPlayList(
        artists: LongArray,
        playList: PlayList
    ): Single<List<Composition>> {
        return libraryRepository.getAllCompositionsByArtistIds(artists.asIterable())
            .flatMap { compositions ->
                playListsInteractor.addCompositionsToPlayList(compositions, playList)
                    .toSingleDefault(compositions)
            }
    }

    fun getAllCompositionsForArtists(artists: List<Artist>): Single<List<Composition>> {
        return libraryRepository.getAllCompositionsByArtists(artists)
    }

}