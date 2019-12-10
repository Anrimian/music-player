package com.github.anrimian.musicplayer.domain.business.editor;

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.nullIfEmpty;

public class CompositionEditorInteractor {

    private final EditorRepository editorRepository;
    private final MusicProviderRepository musicProviderRepository;

    public CompositionEditorInteractor(EditorRepository editorRepository,
                                       MusicProviderRepository musicProviderRepository) {
        this.editorRepository = editorRepository;
        this.musicProviderRepository = musicProviderRepository;
    }

    public Completable editCompositionGenre(FullComposition composition, String newGenre) {
        return editorRepository.changeCompositionGenre(composition, newGenre);
    }

    public Completable editCompositionAuthor(FullComposition composition, String newAuthor) {
        return editorRepository.changeCompositionAuthor(composition, nullIfEmpty(newAuthor));
    }

    public Completable editCompositionAlbum(FullComposition composition, String newAlbum) {
        return editorRepository.changeCompositionAlbum(composition, nullIfEmpty(newAlbum));
    }

    public Completable editCompositionAlbumArtist(FullComposition composition, String newArtist) {
        return editorRepository.changeCompositionAlbumArtist(composition, nullIfEmpty(newArtist));
    }

    public Completable editCompositionTitle(FullComposition composition, String newTitle) {
        return editorRepository.changeCompositionTitle(composition, newTitle);
    }

    public Completable editCompositionFileName(FullComposition composition, String newFileName) {
        return editorRepository.changeCompositionFileName(composition, newFileName);
    }

    public Observable<FullComposition> getCompositionObservable(long id) {
        return musicProviderRepository.getCompositionObservable(id);
    }

    public Single<String[]> getAuthorNames() {
        return musicProviderRepository.getAuthorNames();
    }

    public Single<String[]> getAlbumNames() {
        return musicProviderRepository.getAlbumNames();
    }

    public Single<String[]> getGenreNames() {
        return musicProviderRepository.getGenreNames();
    }
}
