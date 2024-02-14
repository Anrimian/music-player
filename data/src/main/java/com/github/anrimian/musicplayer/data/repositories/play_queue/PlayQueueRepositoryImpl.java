package com.github.anrimian.musicplayer.data.repositories.play_queue;

import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.NO_ITEM;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.models.exceptions.TooManyPlayQueueItemsException;
import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.data.models.exceptions.NoCompositionsToInsertException;
import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueData;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;
import com.github.anrimian.musicplayer.domain.utils.rx.CacheFlowable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private final PlayQueueDaoWrapper playQueueDao;
    private final SettingsRepository settingsPreferences;
    private final UiStateRepository uiStatePreferences;
    private final Scheduler scheduler;

    private final BehaviorSubject<Long> playQueueCreateTimeSubject = BehaviorSubject.createDefault(0L);

    private final CacheFlowable<List<PlayQueueItem>> playQueueObservable;
    private final Observable<PlayQueueEvent> currentItemObservable;

    private boolean firstItemEmitted;
    private boolean consumeDeletedItemEvent = false;

    public PlayQueueRepositoryImpl(PlayQueueDaoWrapper playQueueDao,
                                   SettingsRepository settingsPreferences,
                                   UiStateRepository uiStatePreferences,
                                   Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.settingsPreferences = settingsPreferences;
        this.uiStatePreferences = uiStatePreferences;
        this.scheduler = scheduler;

        playQueueObservable = new CacheFlowable<>(
                settingsPreferences.getRandomPlayingObservable()
                        .switchMap(isRandom -> settingsPreferences.getDisplayFileNameObservable()
                                .switchMap(useFileName -> playQueueDao.getPlayQueueObservable(isRandom, useFileName))
                        ).toFlowable(BackpressureStrategy.LATEST),
                new ArrayList<>()
        );

        currentItemObservable = uiStatePreferences.getCurrentItemIdObservable()
                .switchMap(this::getPlayQueueEvent)
                .subscribeOn(scheduler)
                .replay(1)
                .refCount();
    }

    @Override
    public Completable setPlayQueue(List<Long> compositionIds, int startPosition) {
        return Completable.fromAction(() -> insertNewQueue(compositionIds, startPosition))
                .subscribeOn(scheduler);
    }

    @Override
    public Flowable<Integer> getCurrentItemPositionObservable() {
        return Observable.combineLatest(uiStatePreferences.getCurrentItemIdObservable(),
                        settingsPreferences.getRandomPlayingObservable(),
                        playQueueDao::getIndexPositionObservable)
                .switchMap(observable -> observable)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<PlayQueueEvent> getCurrentQueueItemObservable() {
        return currentItemObservable;
    }

    @Override
    public Flowable<List<PlayQueueItem>> getPlayQueueObservable() {
        return playQueueObservable.getFlowable();
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        Completable.fromAction(() -> {
            playQueueObservable.clearCache();
            if (enabled) {
                long itemId = uiStatePreferences.getCurrentQueueItemId();
                playQueueDao.reshuffleQueue(itemId);
            }
            settingsPreferences.setRandomPlayingEnabled(enabled);
        }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Single<Integer> skipToNext() {
        return Single.fromCallable(() -> {
            long currentItemId = uiStatePreferences.getCurrentQueueItemId();
            boolean isShuffled = settingsPreferences.isRandomPlayingEnabled();
            long nextQueueItemId = playQueueDao.getNextQueueItemId(currentItemId, isShuffled);
            setCurrentItem(nextQueueItemId);

            return playQueueDao.getPosition(nextQueueItemId, isShuffled);
        }).subscribeOn(scheduler);
    }

    @Override
    public void skipToPrevious() {
        Completable.fromAction(() -> {
                    long currentItemId = uiStatePreferences.getCurrentQueueItemId();
                    boolean isShuffled = settingsPreferences.isRandomPlayingEnabled();
                    long nextQueueItemId = playQueueDao.getPreviousQueueItemId(currentItemId, isShuffled);
                    setCurrentItem(nextQueueItemId);
                }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void skipToItem(long itemId) {
        setCurrentItem(itemId);
    }

    @Override
    public Completable removeQueueItem(PlayQueueItem item) {
        return Completable.fromAction(() -> playQueueDao.deleteItem(item.getId()))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable restoreDeletedItem() {
        return Completable.fromAction(() -> {
            Long restoredId = playQueueDao.restoreDeletedItem();
            if (uiStatePreferences.getCurrentQueueItemId() == NO_ITEM && restoredId != null) {
                setCurrentItem(restoredId);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable swapItems(PlayQueueItem firstItem,
                                 PlayQueueItem secondItem) {
        return Completable.fromRunnable(() -> playQueueDao.swapItems(firstItem,
                secondItem,
                settingsPreferences.isRandomPlayingEnabled())
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayNext(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            checkPlayQueueItemsCount(compositions.size());
            long id = uiStatePreferences.getCurrentQueueItemId();
            long firstId = playQueueDao.addCompositionsToQueue(compositions, id);
            if (id == NO_ITEM) {
                setCurrentItem(firstId);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToEnd(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            checkPlayQueueItemsCount(compositions.size());
            long id = uiStatePreferences.getCurrentQueueItemId();
            long firstId = playQueueDao.addCompositionsToEndQueue(compositions);
            if (id == NO_ITEM) {
                setCurrentItem(firstId);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Single<Boolean> isCurrentCompositionAtEndOfQueue() {
        return Single.fromCallable(() -> {
            boolean isShuffled = settingsPreferences.isRandomPlayingEnabled();
            int currentPosition = playQueueDao.getPosition(
                    uiStatePreferences.getCurrentQueueItemId(),
                    isShuffled
            );
            return currentPosition == playQueueDao.getLastPosition(isShuffled);
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable clearPlayQueue() {
        return Completable.fromAction(playQueueDao::deletePlayQueue)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<Integer> getPlayQueueSizeObservable() {
        return playQueueDao.getPlayQueueSizeObservable();
    }

    @Override
    public Observable<PlayQueueData> getPlayQueueDataObservable() {
        return Observable.combineLatest(
                settingsPreferences.getRandomPlayingObservable(),
                playQueueCreateTimeSubject,
                PlayQueueData::new
        );
    }

    private void checkPlayQueueItemsCount(int itemsCountToInsert) {
        if (itemsCountToInsert == 0) {
            throw new NoCompositionsToInsertException();
        }
        if (playQueueDao.getPlayQueueSize() + itemsCountToInsert > Constants.PLAY_QUEUE_MAX_ITEMS_COUNT) {
            throw new TooManyPlayQueueItemsException();
        }
    }

    private void setCurrentItem(@Nullable Long itemId) {
        if (itemId == null) {
            itemId = NO_ITEM;
        }
        uiStatePreferences.setCurrentQueueItemId(itemId);
    }

    private void insertNewQueue(List<Long> compositionIds, int startPosition) {
        if (compositionIds.isEmpty()) {
            return;
        }
        if (compositionIds.size() > Constants.PLAY_QUEUE_MAX_ITEMS_COUNT) {
            throw new TooManyPlayQueueItemsException();
        }
        consumeDeletedItemEvent = true;
        long itemId = playQueueDao.insertNewPlayQueue(compositionIds,
                settingsPreferences.isRandomPlayingEnabled(),
                startPosition);
        setCurrentItem(itemId);
        consumeDeletedItemEvent = false;
    }

    private Observable<PlayQueueEvent> getPlayQueueEvent(long id) {
        if (id == NO_ITEM) {
            return Observable.just(new PlayQueueEvent(null));
        }

        return settingsPreferences.getDisplayFileNameObservable()
                .switchMap(useFileName -> playQueueDao.getItemObservable(id, useFileName))
                .flatMap(this::checkForExisting)
                .map(this::mapToQueueEvent);
    }

    private Observable<PlayQueueItem> checkForExisting(Optional<PlayQueueItem> itemOpt) {
        return Observable.<PlayQueueItem>create(emitter -> {
            PlayQueueItem item = itemOpt.getValue();
            if (item == null) {
                if (consumeDeletedItemEvent) {
                    return;
                }
                //handle deleted item
                boolean isRandom = settingsPreferences.isRandomPlayingEnabled();
                int lastPosition = uiStatePreferences.getCurrentItemLastPosition();
                Long nextItemId = playQueueDao.getItemAtPosition(lastPosition, isRandom);
                if (nextItemId == null) {
                    nextItemId = playQueueDao.getItemAtPosition(0, isRandom);
                }
                setCurrentItem(nextItemId);
                return;
            }
            emitter.onNext(item);
        }).flatMap(item -> settingsPreferences.getRandomPlayingObservable()
                .switchMap(random -> playQueueDao.getPositionObservable(item.getId(), random))
                .doOnNext(uiStatePreferences::setCurrentItemLastPosition)
                .map(o -> item));
    }

    private PlayQueueEvent mapToQueueEvent(PlayQueueItem item) {
        long trackPosition = 0;
        if (!firstItemEmitted) {
            firstItemEmitted = true;
            trackPosition = uiStatePreferences.getTrackPosition();
        }
        return new PlayQueueEvent(item, trackPosition);
    }
}
