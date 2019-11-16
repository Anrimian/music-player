package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
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

    public Observable<List<Composition>> getCompositionsByArtist(long artistId) {
        return musicProviderRepository.getCompositionsByArtist(artistId);
    }

    public Observable<Artist> getArtistObservable(long artistId) {
        return musicProviderRepository.getArtistObservable(artistId);
    }

    public Observable<List<Album>> getAllAlbumsForArtist(long artistId) {
        return musicProviderRepository.getAllAlbumsForArtist(artistId);
    }
}
