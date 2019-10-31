package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import java.util.List;

import io.reactivex.Observable;

public class LibraryArtistsInteractor {

    private final MusicProviderRepository musicProviderRepository;

    public LibraryArtistsInteractor(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
    }

    public Observable<List<Artist>> getArtistsObservable() {
        return musicProviderRepository.getArtistsObservable();
    }
}
