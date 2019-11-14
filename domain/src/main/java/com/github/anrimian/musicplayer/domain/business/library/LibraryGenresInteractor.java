package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import java.util.List;

import io.reactivex.Observable;

public class LibraryGenresInteractor {

    private final MusicProviderRepository musicProviderRepository;

    public LibraryGenresInteractor(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
    }

    public Observable<List<Genre>> getGenresObservable() {
        return musicProviderRepository.getGenresObservable();
    }
}
