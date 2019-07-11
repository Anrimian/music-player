package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.repositories.CompositionEditorRepository;

import io.reactivex.Completable;
import io.reactivex.Scheduler;

public class CompositionEditorRepositoryImpl implements CompositionEditorRepository {

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor();
    private final Scheduler scheduler;

    public CompositionEditorRepositoryImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Completable editCompositionAuthor(Composition composition, String newAuthor) {
        return sourceEditor.setCompositionAuthor(composition.getFilePath(), newAuthor)//update media store
                .subscribeOn(scheduler);
    }
}
