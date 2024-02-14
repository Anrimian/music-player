package com.github.anrimian.musicplayer.domain.interactors.library

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class LibraryGenresInteractor(
    private val playerInteractor: LibraryPlayerInteractor,
    private val libraryRepository: LibraryRepository,
    private val settingsRepository: SettingsRepository,
    private val uiStateRepository: UiStateRepository
) {

    fun getGenresObservable(searchText: String?): Observable<List<Genre>> {
        return libraryRepository.getGenresObservable(searchText)
    }

    fun getGenreItemsObservable(genreId: Long): Observable<List<Composition>> {
        return libraryRepository.getGenreItemsObservable(genreId)
    }

    fun getGenreObservable(genreId: Long): Observable<Genre> {
        return libraryRepository.getGenreObservable(genreId)
    }

    fun getOrder(): Order = settingsRepository.genresOrder

    fun setOrder(order: Order) {
        settingsRepository.genresOrder = order
    }

    fun setSelectedGenreScreen(genreId: Long) {
        uiStateRepository.selectedGenreScreenId = genreId
    }

    fun getSavedListPosition(): ListPosition? = uiStateRepository.savedGenresListPosition

    fun saveListPosition(listPosition: ListPosition?) {
        uiStateRepository.saveGenresListPosition(listPosition)
    }

    fun startPlaying(genres: List<Genre>): Completable {
        return libraryRepository.getCompositionIdsInGenres(genres)
            .flatMapCompletable(playerInteractor::setQueueAndPlay)
    }

    fun getCompositionsInGenresIds(genreIds: LongArray): Single<List<Composition>> {
        return libraryRepository.getCompositionsInGenresIds(genreIds.asIterable())
    }

    fun getCompositionsInGenres(genres: List<Genre>): Single<List<Composition>> {
        return libraryRepository.getCompositionsInGenres(genres)
    }

}