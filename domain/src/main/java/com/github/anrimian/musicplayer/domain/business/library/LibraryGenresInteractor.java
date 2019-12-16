package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

public class LibraryGenresInteractor {

    private final MusicProviderRepository musicProviderRepository;

    public LibraryGenresInteractor(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
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
}
