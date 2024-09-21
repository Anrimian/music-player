package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueData
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Created on 16.04.2018.
 */
interface PlayQueueRepository {

    fun setPlayQueue(compositionIds: List<Long>, startPosition: Int): Completable

    fun getCurrentItemPositionObservable(): Flowable<Int>

    fun getCurrentQueueItemObservable(): Observable<PlayQueueEvent>

    fun getNextQueueItemId(): Single<Long>

    fun getPlayQueueObservable(): Flowable<List<PlayQueueItem>>

    fun setRandomPlayingEnabled(enabled: Boolean)

    fun skipToNext(): Single<Int>

    fun skipToPrevious()

    fun skipToItem(itemId: Long)

    fun removeQueueItem(item: PlayQueueItem): Completable

    fun restoreDeletedItem(): Completable

    fun swapItems(first: PlayQueueItem, second: PlayQueueItem): Completable

    fun addCompositionsToPlayNext(compositions: List<Composition>): Completable

    fun addCompositionsToEnd(compositions: List<Composition>): Completable

    fun isCurrentCompositionAtEndOfQueue(): Single<Boolean>

    fun clearPlayQueue(): Completable

    fun getPlayQueueSizeObservable(): Observable<Int>

    fun getPlayQueueDataObservable(): Observable<PlayQueueData>

    fun setCurrentItemTrackPosition(trackPosition: Long): Completable

    fun getCurrentItemTrackPosition(): Single<Long>

    fun setItemTrackPosition(itemId: Long, trackPosition: Long): Completable

    fun getItemTrackPosition(itemId: Long): Single<Long>

}
