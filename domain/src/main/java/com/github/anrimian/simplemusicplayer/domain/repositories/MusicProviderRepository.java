package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.player.error.ErrorType;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface MusicProviderRepository {

    Single<List<Composition>> getAllCompositions();

    Single<Folder> getCompositionsInPath(@Nullable String path);

    Single<List<Composition>> getAllCompositionsInPath(@Nullable String path);

    Completable writeErrorAboutComposition(ErrorType errorType, Composition composition);

    Completable deleteComposition(Composition composition);
}
