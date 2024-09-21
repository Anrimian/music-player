package com.github.anrimian.musicplayer.data.repositories.play_queue

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper
import com.github.anrimian.musicplayer.data.models.exceptions.NoCompositionsToInsertException
import com.github.anrimian.musicplayer.data.models.exceptions.TooManyPlayQueueItemsException
import com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueData
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.domain.utils.rx.CacheFlowable
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

class PlayQueueRepositoryImpl(
    private val playQueueDao: PlayQueueDaoWrapper,
    private val settingsPreferences: SettingsRepository,
    private val uiStatePreferences: UiStateRepository,
    private val scheduler: Scheduler
) : PlayQueueRepository {

    private val playQueueCreateTimeSubject = BehaviorSubject.createDefault(0L)

    private val playQueueObservable = CacheFlowable(
        settingsPreferences.randomPlayingObservable
            .switchMap(
                { isRandom ->
                    settingsPreferences.displayFileNameObservable
                        .switchMap { useFileName ->
                            playQueueDao.getPlayQueueObservable(isRandom, useFileName)
                        }
                }
            ).toFlowable(BackpressureStrategy.LATEST),
        ArrayList()
    )

    private val currentItemObservable = uiStatePreferences.currentItemIdObservable
        .switchMap(this::getPlayQueueEvent)
        .subscribeOn(scheduler)
        .replay(1)
        .refCount()

    private var consumeDeletedItemEvent = false

    override fun setPlayQueue(compositionIds: List<Long>, startPosition: Int): Completable {
        return Completable.fromAction { insertNewQueue(compositionIds, startPosition) }
            .subscribeOn(scheduler)
    }

    override fun getCurrentItemPositionObservable(): Flowable<Int> {
        return Observable.combineLatest(
            uiStatePreferences.currentItemIdObservable,
            settingsPreferences.randomPlayingObservable,
            playQueueDao::getIndexPositionObservable
        ).switchMap { observable -> observable }
            .toFlowable(BackpressureStrategy.LATEST)
    }

    override fun getCurrentQueueItemObservable(): Observable<PlayQueueEvent> {
        return currentItemObservable
    }

    override fun getNextQueueItemId(): Single<Long> {
        return Single.fromCallable {
            val currentItemId = uiStatePreferences.currentQueueItemId
            val isShuffled = settingsPreferences.isRandomPlayingEnabled
            playQueueDao.getNextQueueItemId(currentItemId, isShuffled)
        }.subscribeOn(scheduler)
    }

    override fun getPlayQueueObservable(): Flowable<List<PlayQueueItem>> {
        return playQueueObservable.getFlowable()
    }

    override fun setRandomPlayingEnabled(enabled: Boolean) {
        Completable.fromAction {
            playQueueObservable.clearCache()
            if (enabled) {
                val itemId = uiStatePreferences.currentQueueItemId
                playQueueDao.reshuffleQueue(itemId)
            }
            settingsPreferences.isRandomPlayingEnabled = enabled
        }.subscribeOn(scheduler)
            .subscribe()
    }

    override fun skipToNext(): Single<Int> {
        return Single.fromCallable {
            val currentItemId = uiStatePreferences.currentQueueItemId
            val isShuffled = settingsPreferences.isRandomPlayingEnabled
            val nextQueueItemId = playQueueDao.getNextQueueItemId(currentItemId, isShuffled)
            setCurrentItem(nextQueueItemId)
            playQueueDao.getPosition(nextQueueItemId, isShuffled)
        }.subscribeOn(scheduler)
    }

    override fun skipToPrevious() {
        Completable.fromAction {
            val currentItemId = uiStatePreferences.currentQueueItemId
            val isShuffled = settingsPreferences.isRandomPlayingEnabled
            val nextQueueItemId = playQueueDao.getPreviousQueueItemId(currentItemId, isShuffled)
            setCurrentItem(nextQueueItemId)
        }.subscribeOn(scheduler)
            .subscribe()
    }

    override fun skipToItem(itemId: Long) {
        setCurrentItem(itemId)
    }

    override fun removeQueueItem(item: PlayQueueItem): Completable {
        return Completable.fromAction { playQueueDao.deleteItem(item.itemId) }
            .subscribeOn(scheduler)
    }

    override fun restoreDeletedItem(): Completable {
        return Completable.fromAction {
            val restoredId = playQueueDao.restoreDeletedItem()
            if (uiStatePreferences.currentQueueItemId == UiStateRepositoryImpl.NO_ITEM && restoredId != null) {
                setCurrentItem(restoredId)
            }
        }.subscribeOn(scheduler)
    }

    override fun swapItems(
        first: PlayQueueItem,
        second: PlayQueueItem
    ): Completable {
        return Completable.fromRunnable {
            playQueueDao.swapItems(first, second, settingsPreferences.isRandomPlayingEnabled)
        }.subscribeOn(scheduler)
    }

    override fun addCompositionsToPlayNext(compositions: List<Composition>): Completable {
        return Completable.fromRunnable {
            checkPlayQueueItemsCount(compositions.size)
            val id = uiStatePreferences.currentQueueItemId
            val firstId = playQueueDao.addCompositionsToQueue(compositions, id)
            if (id == UiStateRepositoryImpl.NO_ITEM) {
                setCurrentItem(firstId)
            }
        }.subscribeOn(scheduler)
    }

    override fun addCompositionsToEnd(compositions: List<Composition>): Completable {
        return Completable.fromRunnable {
            checkPlayQueueItemsCount(compositions.size)
            val id = uiStatePreferences.currentQueueItemId
            val firstId = playQueueDao.addCompositionsToEndQueue(compositions)
            if (id == UiStateRepositoryImpl.NO_ITEM) {
                setCurrentItem(firstId)
            }
        }.subscribeOn(scheduler)
    }

    override fun isCurrentCompositionAtEndOfQueue(): Single<Boolean> {
        return Single.fromCallable {
            val isShuffled = settingsPreferences.isRandomPlayingEnabled
            val currentPosition = playQueueDao.getPosition(
                uiStatePreferences.currentQueueItemId,
                isShuffled
            )
            currentPosition == playQueueDao.getLastPosition(isShuffled)
        }.subscribeOn(scheduler)
    }

    override fun clearPlayQueue(): Completable {
        return Completable.fromAction { playQueueDao.deletePlayQueue() }
            .subscribeOn(scheduler)
    }

    override fun getPlayQueueSizeObservable(): Observable<Int> {
        return playQueueDao.getPlayQueueSizeObservable()
    }

    override fun getPlayQueueDataObservable(): Observable<PlayQueueData> {
        return Observable.combineLatest(
            settingsPreferences.randomPlayingObservable,
            playQueueCreateTimeSubject,
            ::PlayQueueData
        )
    }

    override fun setCurrentItemTrackPosition(trackPosition: Long): Completable {
        return Completable.fromAction {
            val itemId = uiStatePreferences.currentQueueItemId
            playQueueDao.insertTrackPosition(itemId, trackPosition)
        }.subscribeOn(scheduler)
    }

    override fun getCurrentItemTrackPosition(): Single<Long> {
        return Single.fromCallable {
            val itemId = uiStatePreferences.currentQueueItemId
            playQueueDao.getTrackPosition(itemId)
        }.subscribeOn(scheduler)
    }

    override fun setItemTrackPosition(itemId: Long, trackPosition: Long): Completable {
        return Completable.fromAction {
            playQueueDao.insertTrackPosition(itemId, trackPosition)
        }.subscribeOn(scheduler)
    }

    override fun getItemTrackPosition(itemId: Long): Single<Long> {
        return Single.fromCallable { playQueueDao.getTrackPosition(itemId) }
            .subscribeOn(scheduler)
    }

    private fun checkPlayQueueItemsCount(itemsCountToInsert: Int) {
        if (itemsCountToInsert == 0) {
            throw NoCompositionsToInsertException()
        }
        if (playQueueDao.getPlayQueueSize() + itemsCountToInsert > Constants.PLAY_QUEUE_MAX_ITEMS_COUNT) {
            throw TooManyPlayQueueItemsException()
        }
    }

    private fun setCurrentItem(itemId: Long?) {
        uiStatePreferences.currentQueueItemId = itemId ?: UiStateRepositoryImpl.NO_ITEM
    }

    private fun insertNewQueue(compositionIds: List<Long>, startPosition: Int) {
        if (compositionIds.isEmpty()) {
            return
        }
        if (compositionIds.size > Constants.PLAY_QUEUE_MAX_ITEMS_COUNT) {
            throw TooManyPlayQueueItemsException()
        }
        consumeDeletedItemEvent = true
        val itemId = playQueueDao.insertNewPlayQueue(
            compositionIds,
            settingsPreferences.isRandomPlayingEnabled,
            startPosition
        )
        setCurrentItem(itemId)
        consumeDeletedItemEvent = false
    }

    private fun getPlayQueueEvent(id: Long): Observable<PlayQueueEvent> {
        if (id == UiStateRepositoryImpl.NO_ITEM) {
            return Observable.just(PlayQueueEvent(null))
        }
        return settingsPreferences.displayFileNameObservable
            .switchMap { useFileName -> playQueueDao.getItemObservable(id, useFileName) }
            .flatMap(::checkForExisting)
            .map(::PlayQueueEvent)
    }

    private fun checkForExisting(itemOpt: Optional<PlayQueueItem>): Observable<PlayQueueItem> {
        return Observable.create { emitter ->
            val item = itemOpt.value
            if (item == null) {
                if (consumeDeletedItemEvent) {
                    return@create
                }
                //handle deleted item
                val isRandom = settingsPreferences.isRandomPlayingEnabled
                val lastPosition = uiStatePreferences.currentItemLastPosition
                var nextItemId = playQueueDao.getItemAtPosition(lastPosition, isRandom)
                if (nextItemId == null) {
                    nextItemId = playQueueDao.getItemAtPosition(0, isRandom)
                }
                setCurrentItem(nextItemId)
                return@create
            }
            emitter.onNext(item)
        }.flatMap { item ->
            settingsPreferences.randomPlayingObservable
                .switchMap { random -> playQueueDao.getPositionObservable(item.itemId, random) }
                .doOnNext { position -> uiStatePreferences.currentItemLastPosition = position }
                .map { item }
        }
    }
}
