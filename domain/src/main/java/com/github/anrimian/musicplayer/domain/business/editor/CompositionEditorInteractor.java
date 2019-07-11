package com.github.anrimian.musicplayer.domain.business.editor;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.repositories.CompositionEditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class CompositionEditorInteractor {

    private final CompositionEditorRepository editorRepository;
    private final MusicProviderRepository musicProviderRepository;

    public CompositionEditorInteractor(CompositionEditorRepository editorRepository,
                                       MusicProviderRepository musicProviderRepository) {
        this.editorRepository = editorRepository;
        this.musicProviderRepository = musicProviderRepository;
    }

    public Completable editCompositionAuthor(Composition composition, String newAuthor) {
        return editorRepository.editCompositionAuthor(composition, newAuthor);
    }

    public Observable<Composition> getCompositionObservable(long id) {
        return musicProviderRepository.getCompositionObservable(id);
    }
}
