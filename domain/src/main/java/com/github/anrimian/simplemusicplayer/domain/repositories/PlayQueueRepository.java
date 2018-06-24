package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 16.04.2018.
 */
public interface PlayQueueRepository {

    Completable setPlayQueue(List<Composition> compositions);

    Observable<CurrentComposition> getCurrentCompositionObservable();

    Observable<List<Composition>> getPlayQueueObservable();

    Observable<Change<List<Composition>>> getPlayQueueChangeObservable();

    Observable<Change<Composition>> getCompositionChangeObservable();

    void setRandomPlayingEnabled(boolean enabled);

    Single<Integer> skipToNext();

    Single<Integer> skipToPrevious();

    Completable skipToPosition(int position);
}
