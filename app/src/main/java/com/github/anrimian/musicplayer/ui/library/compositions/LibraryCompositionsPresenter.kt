package com.github.anrimian.musicplayer.ui.library.compositions

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryCompositionsInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler

class LibraryCompositionsPresenter(
    private val interactor: LibraryCompositionsInteractor,
    playerInteractor: LibraryPlayerInteractor,
    displaySettingsInteractor: DisplaySettingsInteractor,
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    playListsInteractor: PlayListsInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
) : BaseLibraryCompositionsPresenter<Composition, LibraryCompositionsView>(
    displaySettingsInteractor,
    syncInteractor,
    playerInteractor,
    playListsInteractor,
    errorParser,
    uiScheduler
) {
    override fun getCompositionsObservable(searchText: String?): Observable<List<Composition>> {
        return interactor.getCompositionsObservable(searchText)
    }

    override fun getSavedListPosition(): ListPosition? {
        return interactor.getSavedListPosition()
    }

    override fun saveListPosition(listPosition: ListPosition) {
        interactor.saveListPosition(listPosition)
    }

    fun onOrderMenuItemClicked() {
        viewState.showSelectOrderScreen(interactor.getOrder())
    }

    fun onOrderSelected(order: Order) {
        interactor.setOrder(order)
    }
}