package com.github.anrimian.musicplayer.domain.interactors.library

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Observable

class LibraryCompositionsInteractor(
    private val musicProviderRepository: LibraryRepository,
    private val settingsRepository: SettingsRepository,
    private val uiStateRepository: UiStateRepository
) {

    fun getCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return musicProviderRepository.getAllCompositionsObservable(searchText)
    }

    fun setOrder(order: Order) {
        settingsRepository.compositionsOrder = order
    }

    fun getOrder(): Order {
        return settingsRepository.compositionsOrder
    }

    fun getSavedListPosition(): ListPosition? {
        return uiStateRepository.savedCompositionsListPosition
    }

    fun saveListPosition(listPosition: ListPosition?) {
        uiStateRepository.saveCompositionsListPosition(listPosition)
    }

}