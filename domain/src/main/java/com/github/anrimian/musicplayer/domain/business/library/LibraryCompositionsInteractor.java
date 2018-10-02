package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class LibraryCompositionsInteractor {

    private final MusicProviderRepository musicProviderRepository;
    private final MusicPlayerInteractor musicPlayerInteractor;
    private final SettingsRepository settingsRepository;

    public LibraryCompositionsInteractor(MusicProviderRepository musicProviderRepository,
                                         MusicPlayerInteractor musicPlayerInteractor,
                                         SettingsRepository settingsRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.settingsRepository = settingsRepository;
    }

    public Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return musicProviderRepository.getAllCompositionsObservable(searchText);
    }

    public Completable play(List<Composition> list) {
        return musicPlayerInteractor.startPlaying(list);
    }

    public Completable deleteCompositions(List<Composition> compositions) {
        return musicProviderRepository.deleteCompositions(compositions);
    }

    public void setOrder(Order order) {
        settingsRepository.setCompositionsOrder(order);
    }

    public Order getOrder() {
        return settingsRepository.getCompositionsOrder();
    }
}
