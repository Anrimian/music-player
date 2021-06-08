package com.github.anrimian.musicplayer.domain.interactors.library;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class LibraryCompositionsInteractor {

    private final LibraryRepository musicProviderRepository;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;

    public LibraryCompositionsInteractor(LibraryRepository musicProviderRepository,
                                         SettingsRepository settingsRepository,
                                         UiStateRepository uiStateRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
    }

    public Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return musicProviderRepository.getAllCompositionsObservable(searchText);
    }

    public void setOrder(Order order) {
        settingsRepository.setCompositionsOrder(order);
    }

    public Order getOrder() {
        return settingsRepository.getCompositionsOrder();
    }

    public ListPosition getSavedListPosition() {
        return uiStateRepository.getSavedCompositionsListPosition();
    }

    public void saveListPosition(ListPosition listPosition) {
        uiStateRepository.saveCompositionsListPosition(listPosition);
    }
}
