package com.github.anrimian.simplemusicplayer.domain.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 16.04.2018.
 */
public interface PlayQueueRepository {

    Completable setPlayQueue(List<Composition> compositions);

    int getCompositionPosition(@Nonnull Composition composition);

    Observable<CompositionEvent> getCurrentCompositionObservable();

    Observable<List<Composition>> getPlayQueueObservable();

    void setRandomPlayingEnabled(boolean enabled);

    Single<Integer> skipToNext();

    Single<Integer> skipToPrevious();

    Completable skipToPosition(int position);
}
