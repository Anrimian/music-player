package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import io.reactivex.Completable;

public interface CompositionEditorRepository {

    Completable editCompositionAuthor(Composition composition, String newAuthor);

    Completable editCompositionTitle(Composition composition, String title);
}
