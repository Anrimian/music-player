package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

public class LibraryGenresInteractor {

    private final MusicProviderRepository musicProviderRepository;
    private final SettingsRepository settingsRepository;

    public LibraryGenresInteractor(MusicProviderRepository musicProviderRepository,
                                   SettingsRepository settingsRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.settingsRepository = settingsRepository;
    }

    public Observable<List<Genre>> getGenresObservable(@Nullable String searchText) {
        return musicProviderRepository.getGenresObservable(searchText);
    }

    public Observable<List<Composition>> getGenreItemsObservable(long genreId) {
        return musicProviderRepository.getGenreItemsObservable(genreId);
    }

    public Observable<Genre> getGenreObservable(long genreId) {
        return musicProviderRepository.getGenreObservable(genreId);
    }

    public void setOrder(Order order) {
        settingsRepository.setGenresOrder(order);
    }

    public Order getOrder() {
        return settingsRepository.getGenresOrder();
    }
}
