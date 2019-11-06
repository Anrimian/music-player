package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import java.util.List;

import io.reactivex.Observable;

public class LibraryAlbumsInteractor {

    private final MusicProviderRepository musicProviderRepository;

    public LibraryAlbumsInteractor(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
    }

    public Observable<List<Album>> getAlbumssObservable() {
        return musicProviderRepository.getAlbumsObservable();
    }
}
