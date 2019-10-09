package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 16.04.2018.
 */
public interface PlayQueueRepository {

    Completable setPlayQueue(List<Composition> compositions);

    Completable setPlayQueue(List<Composition> compositions, int startPosition);

    Maybe<Integer> getCompositionPosition(@Nonnull PlayQueueItem playQueueItem);

    Observable<PlayQueueEvent> getCurrentQueueItemObservable();

    Flowable<List<PlayQueueItem>> getPlayQueueObservable();

    void setRandomPlayingEnabled(boolean enabled);

    Single<Integer> skipToNext();

    Single<Integer> skipToPrevious();

    Completable skipToPosition(int position);

    Completable removeQueueItem(PlayQueueItem item);

    Completable swapItems(PlayQueueItem firstItem,
                          int firstPosition,
                          PlayQueueItem secondItem,
                          int secondPosition);

    Completable addCompositionsToPlayNext(List<Composition> compositions);

    Completable addCompositionsToEnd(List<Composition> compositions);
}
