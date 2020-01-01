package com.github.anrimian.musicplayer.data.repositories.play_queue;

import android.util.Log;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueCompositionDto;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.utils.collections.IndexedList;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.NO_ITEM;
import static com.github.anrimian.musicplayer.domain.Constants.NO_POSITION;

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private final PlayQueueDaoWrapper playQueueDao;
    private final SettingsRepository settingsPreferences;
    private final UiStatePreferences uiStatePreferences;
    private final Scheduler scheduler;

    private final Flowable<List<PlayQueueItem>> playQueueObservable;
    private final Observable<PlayQueueEvent> currentItemObservable;

    @Deprecated
    private final PlayQueueCache queueCache;//we really need this? can be moved in db

    private boolean firstItemEmitted;

    public PlayQueueRepositoryImpl(PlayQueueDaoWrapper playQueueDao,
                                   SettingsRepository settingsPreferences,
                                   UiStatePreferences uiStatePreferences,
                                   Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.settingsPreferences = settingsPreferences;
        this.uiStatePreferences = uiStatePreferences;
        this.scheduler = scheduler;

        queueCache = new PlayQueueCache(() -> {
            boolean isRandom = settingsPreferences.isRandomPlayingEnabled();
            List<PlayQueueItem> items = playQueueDao.getPlayQueue(isRandom);
            return new IndexedList<>(items);
        });

        playQueueObservable = settingsPreferences.getRandomPlayingObservable()
                .doOnNext(shuffled -> Log.d("KEK2", "new shuffled mode: " + shuffled))
                .switchMap(playQueueDao::getPlayQueueObservable)
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(list -> {
                    IndexedList<PlayQueueItem> newQueue = new IndexedList<>(list);
                    checkForCurrentItemInNewQueue(newQueue);
                    queueCache.updateQueue(newQueue);
                }).replay(1)
                .refCount();

        currentItemObservable = uiStatePreferences.getCurrentItemIdObservable()
                .switchMap(this::getPlayQueueEvent)
                .doOnNext(item -> Log.d("KEK3", "new item: " + item.getPlayQueueItem()))
                .subscribeOn(scheduler)
                .replay(1)
                .refCount();
    }

    @Override
    public Completable setPlayQueue(List<Composition> compositions) {
        return setPlayQueue(compositions, NO_POSITION);
    }

    @Override
    public Completable setPlayQueue(List<Composition> compositions, int startPosition) {
        if (compositions.isEmpty()) {
            return Completable.complete();
        }
        return Single.fromCallable(() -> playQueueDao.insertNewPlayQueue(compositions,
                settingsPreferences.isRandomPlayingEnabled(),
                startPosition)
        ).doOnSuccess(this::setCurrentItem)
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    @Nullable
    @Override
    public Maybe<Integer> getCompositionPosition(@Nonnull PlayQueueItem playQueueItem) {
        return Maybe.fromCallable(() -> queueCache.getCurrentQueue().indexOf(playQueueItem))
                .subscribeOn(scheduler);
    }

    @Override
    public int getCurrentPosition() {
        IndexedList<PlayQueueItem> currentQueue = queueCache.getCurrentQueue();
        //noinspection ConstantConditions
        if (currentQueue == null) {//hotfix, refactor and solve
            return 0;
        }
        return currentQueue.indexOf(getCurrentItem());
    }

    @Override
    public Observable<PlayQueueEvent> getCurrentQueueItemObservable() {//wrong emits...?
        return currentItemObservable;
    }

    @Override
    public Flowable<List<PlayQueueItem>> getPlayQueueObservable() {
        return playQueueObservable;
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {//performance issue with large lists
        Completable.fromAction(() -> {
            if (enabled) {
                PlayQueueItem item = getCurrentItem();
                playQueueDao.reshuffleQueue(item);
                Log.d("KEK2", "reshuffleQueue");
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
    public void skipToPosition(int position) {
        Completable.fromAction(() -> {
            boolean isShuffled = settingsPreferences.isRandomPlayingEnabled();
            long itemId = playQueueDao.getItemAtPosition(position, isShuffled);
            setCurrentItem(itemId);
        }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Completable removeQueueItem(PlayQueueItem item) {
        return Completable.fromAction(() -> {
//            IndexedList<PlayQueueItem> currentQueue = queueCache.getCurrentQueue();
//            Integer currentPosition = null;
//            PlayQueueItem currentItem = getCurrentItem();
//            if (item.equals(currentItem)) {
//                currentPosition = currentQueue.indexOf(currentItem);
//            }
            playQueueDao.deleteItem(item.getId());

//            if (currentPosition != null) {
//                selectItemAt(currentQueue, currentPosition + 1);
//            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable swapItems(PlayQueueItem firstItem,
                                 int firstPosition,
                                 PlayQueueItem secondItem,
                                 int secondPosition) {
        return Completable.fromRunnable(() -> playQueueDao.swapItems(firstItem,
                firstPosition,
                secondItem,
                secondPosition,
                settingsPreferences.isRandomPlayingEnabled())
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayNext(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            PlayQueueItem currentItem = getCurrentItem();
            List<PlayQueueItem> list = playQueueDao.addCompositionsToQueue(compositions, currentItem);
            if (currentItem == null) {
                setCurrentItem(list.get(0));
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToEnd(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            PlayQueueItem currentItem = getCurrentItem();
            List<PlayQueueItem> list = playQueueDao.addCompositionsToEndQueue(compositions);
            if (currentItem == null) {
                setCurrentItem(list.get(0));
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public int getQueueSize() {
        return queueCache.getCurrentQueue().size();
    }

    @Nullable
    private PlayQueueItem getSavedQueueItem() {
        long id = uiStatePreferences.getCurrentQueueItemId();
        PlayQueueCompositionDto entity = playQueueDao.getPlayQueueItem(id);
        if (entity == null) {
            return null;
        }
        return toPlayQueueItem(entity);
    }

    @Deprecated
    private void setCurrentItem(@Nullable PlayQueueItem item) {
        long itemId = NO_ITEM;
        if (item != null) {
            itemId = item.getId();
        }
        uiStatePreferences.setCurrentQueueItemId(itemId);
    }

    private void setCurrentItem(@Nullable Long itemId) {
        if (itemId == null) {
            itemId = NO_ITEM;
        }
        uiStatePreferences.setCurrentQueueItemId(itemId);
    }

    @Nullable
    private PlayQueueItem getCurrentItem() {
        return getSavedQueueItem();
    }

    private PlayQueueItem toPlayQueueItem(PlayQueueCompositionDto entity) {
        return new PlayQueueItem(entity.getItemId(),
                CompositionMapper.toComposition(entity.getComposition())
        );
    }

    private void selectItemAt(IndexedList<PlayQueueItem> queue, int position) {
        PlayQueueItem newItem = null;
        if (queue.size() > 1) {
            if (position >= queue.size()) {
                position = 0;
            }
            newItem = queue.get(position);
        }
        setCurrentItem(newItem);
    }

    private void checkForCurrentItemInNewQueue(IndexedList<PlayQueueItem> newQueue) {
        IndexedList<PlayQueueItem> currentQueue = queueCache.getCurrentQueue();
        PlayQueueItem currentItem = getCurrentItem();
        if (currentItem != null && newQueue.contains(currentItem)) {
            //check for update
//            Integer currentPosition = newQueue.indexOf(currentItem);
//            PlayQueueItem newItem = newQueue.get(currentPosition);
//            if (!PlayQueueItemHelper.areSourcesTheSame(newItem, currentItem)) {
//                currentCompositionSubject.onNext(new PlayQueueEvent(newItem, true));
//            }
        } else {
            //item not found, select new item on this position
            Integer currentPosition = currentQueue.indexOf(currentItem);
            if (currentPosition == null) {
                currentPosition = 0;
            }
            selectItemAt(newQueue, currentPosition);
        }
    }

    private Observable<PlayQueueEvent> getPlayQueueEvent(long id) {
        if (id == NO_ITEM) {
            return Observable.just(new PlayQueueEvent(null));
        }
        return playQueueDao.getItemObservable(id)
                .map(item -> {
                    if (!firstItemEmitted) {
                        firstItemEmitted = true;
                        return new PlayQueueEvent(item, uiStatePreferences.getTrackPosition());
                    }
                    return new PlayQueueEvent(item);
                });
    }
}
