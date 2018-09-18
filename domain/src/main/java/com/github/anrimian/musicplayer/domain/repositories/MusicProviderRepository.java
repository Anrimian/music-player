package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface MusicProviderRepository {

    Observable<List<Composition>> getAllCompositionsObservable();

    Single<Folder> getCompositionsInPath(@Nullable String path);

    Single<List<Composition>> getAllCompositionsInPath(@Nullable String path);

    Completable writeErrorAboutComposition(ErrorType errorType, Composition composition);

    Completable deleteComposition(Composition composition);

    Completable deleteCompositions(List<Composition> compositions);
}
