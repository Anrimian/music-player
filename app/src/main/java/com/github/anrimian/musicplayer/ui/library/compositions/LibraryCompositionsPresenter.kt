package com.github.anrimian.musicplayer.ui.library.compositions

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryCompositionsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler

class LibraryCompositionsPresenter(
    private val interactor: LibraryCompositionsInteractor,
    playListsInteractor: PlayListsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    displaySettingsInteractor: DisplaySettingsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryCompositionsPresenter<LibraryCompositionsView>(
    playerInteractor,
    playListsInteractor,
    displaySettingsInteractor,
    errorParser,
    uiScheduler
) {
    override fun getCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return interactor.getCompositionsObservable(searchText)
    }

    override fun getSavedListPosition(): ListPosition? {
        return interactor.savedListPosition
    }

    override fun saveListPosition(listPosition: ListPosition) {
        interactor.saveListPosition(listPosition)
    }

    fun onOrderMenuItemClicked() {
        viewState.showSelectOrderScreen(interactor.order)
    }

    fun onOrderSelected(order: Order) {
        interactor.order = order
        subscribeOnCompositions()
    }
}