package com.github.anrimian.musicplayer.domain.business.editor;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class CompositionEditorInteractor {

    private final EditorRepository editorRepository;
    private final MusicProviderRepository musicProviderRepository;

    public CompositionEditorInteractor(EditorRepository editorRepository,
                                       MusicProviderRepository musicProviderRepository) {
        this.editorRepository = editorRepository;
        this.musicProviderRepository = musicProviderRepository;
    }

    public Completable editCompositionAuthor(Composition composition, String newAuthor) {
        return editorRepository.changeCompositionAuthor(composition, newAuthor);
    }

    public Completable editCompositionAlbum(Composition composition, String newAlbum) {
        return editorRepository.changeCompositionAlbum(composition, newAlbum);
    }

    public Completable editCompositionTitle(Composition composition, String newTitle) {
        return editorRepository.changeCompositionTitle(composition, newTitle);
    }

    public Completable editCompositionFileName(Composition composition, String newFileName) {
        return editorRepository.changeCompositionFileName(composition, newFileName);
    }

    public Observable<Composition> getCompositionObservable(long id) {
        return musicProviderRepository.getCompositionObservable(id);
    }

    public Single<String[]> getAuthorNames() {
        return musicProviderRepository.getAuthorNames();
    }

    public Single<String[]> getAlbumNames() {
        return musicProviderRepository.getAlbumNames();
    }
}
