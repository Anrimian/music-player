package com.github.anrimian.musicplayer.ui.library.compositions;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryCompositionsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;


public class LibraryCompositionsPresenter
        extends BaseLibraryCompositionsPresenter<LibraryCompositionsView> {

    private final LibraryCompositionsInteractor interactor;

    public LibraryCompositionsPresenter(LibraryCompositionsInteractor interactor,
                                        PlayListsInteractor playListsInteractor,
                                        LibraryPlayerInteractor playerInteractor,
                                        DisplaySettingsInteractor displaySettingsInteractor,
                                        ErrorParser errorParser,
                                        Scheduler uiScheduler) {
        super(playerInteractor, playListsInteractor, displaySettingsInteractor, errorParser, uiScheduler);
        this.interactor = interactor;
    }

    @NonNull
    @Override
    protected Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return interactor.getCompositionsObservable(searchText);
    }

    @Override
    protected ListPosition getSavedListPosition() {
        return interactor.getSavedListPosition();
    }

    @Override
    protected void saveListPosition(ListPosition listPosition) {
        interactor.saveListPosition(listPosition);
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setOrder(order);
        subscribeOnCompositions();
    }
}
