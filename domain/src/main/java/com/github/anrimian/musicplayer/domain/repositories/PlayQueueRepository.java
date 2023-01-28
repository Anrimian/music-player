package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueData;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * Created on 16.04.2018.
 */
public interface PlayQueueRepository {

    Completable setPlayQueue(List<Composition> compositions);

    Completable setPlayQueue(List<Composition> compositions, int startPosition);

    Flowable<Integer> getCurrentItemPositionObservable();

    Observable<PlayQueueEvent> getCurrentQueueItemObservable();

    Flowable<List<PlayQueueItem>> getPlayQueueObservable();

    void setRandomPlayingEnabled(boolean enabled);

    Single<Integer> skipToNext();

    void skipToPrevious();

    void skipToItem(long itemId);

    Completable removeQueueItem(PlayQueueItem item);

    Completable restoreDeletedItem();

    Completable swapItems(PlayQueueItem firstItem, PlayQueueItem secondItem);

    Completable addCompositionsToPlayNext(List<Composition> compositions);

    Completable addCompositionsToEnd(List<Composition> compositions);

    Single<Boolean> isCurrentCompositionAtEndOfQueue();

    Completable clearPlayQueue();

    Observable<Integer> getPlayQueueSizeObservable();

    Observable<PlayQueueData> getPlayQueueDataObservable();
}
