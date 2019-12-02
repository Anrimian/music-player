package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class LibraryAlbumsInteractor {

    private final MusicProviderRepository musicProviderRepository;
    private final EditorRepository editorRepository;

    public LibraryAlbumsInteractor(MusicProviderRepository musicProviderRepository,
                                   EditorRepository editorRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.editorRepository = editorRepository;
    }

    public Observable<List<Album>> getAlbumsObservable() {
        return musicProviderRepository.getAlbumsObservable();
    }

    public Observable<List<Composition>> getAlbumItemsObservable(long albumId) {
        return musicProviderRepository.getAlbumItemsObservable(albumId);
    }

    public Observable<Album> getAlbumObservable(long albumId) {
        return musicProviderRepository.getAlbumObservable(albumId);
    }

    public Completable updateAlbumName(String name, long albumId) {
        return editorRepository.updateAlbumName(name, albumId);
    }
}
