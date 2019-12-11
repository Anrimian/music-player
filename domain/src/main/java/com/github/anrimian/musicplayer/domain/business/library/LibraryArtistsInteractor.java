package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class LibraryArtistsInteractor {

    private final MusicProviderRepository musicProviderRepository;
    private final EditorRepository editorRepository;

    public LibraryArtistsInteractor(MusicProviderRepository musicProviderRepository,
                                    EditorRepository editorRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.editorRepository = editorRepository;
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

    public Completable updateArtistName(String name, long artistId) {
        return editorRepository.updateArtistName(name, artistId);
    }
}
