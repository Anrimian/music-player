package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface MusicProviderRepository {

    Single<List<Composition>> getAllCompositions();

    Completable processErrorWithComposition(Throwable throwable, Composition composition);

    Completable deleteComposition(Composition composition);
}
