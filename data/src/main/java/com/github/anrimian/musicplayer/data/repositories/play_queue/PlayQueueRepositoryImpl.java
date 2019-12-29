package com.github.anrimian.musicplayer.data.repositories.play_queue;

import android.util.Log;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueCompositionDto;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.utils.collections.IndexedList;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.Collections;
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
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.domain.Constants.NO_POSITION;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static io.reactivex.subjects.BehaviorSubject.create;

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private final PlayQueueDaoWrapper playQueueDao;
    private final SettingsRepository settingsPreferences;
    private final UiStatePreferences uiStatePreferences;
    private final Scheduler scheduler;

    private final BehaviorSubject<PlayQueueEvent> currentCompositionSubject = create();

    private final Flowable<List<PlayQueueItem>> playQueueObservable;
    private final PlayQueueCache queueCache;//we really need this? can be moved in db

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
        return Single.fromCallable(() -> playQueueDao.insertNewPlayQueue(compositions))
                .doOnSuccess(playQueue -> selectCurrentItem(playQueue, startPosition))
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
    public Observable<PlayQueueEvent> getCurrentQueueItemObservable() {
        return withDefaultValue(currentCompositionSubject, this::getSavedQueueEvent)
                .subscribeOn(scheduler);
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
            IndexedList<PlayQueueItem> currentQueue = queueCache.getCurrentQueue();
            Integer position = currentQueue.indexOf(getCurrentItem());
            if (position == null) {
                return 0;
            }
            if (position >= currentQueue.size() - 1) {
                position = 0;
            } else {
                position++;
            }
            setCurrentItem(currentQueue.get(position));
            return position;
        }).subscribeOn(scheduler);
    }

    @Override
    public Single<Integer> skipToPrevious() {
        return Single.fromCallable(() -> {
            IndexedList<PlayQueueItem> currentQueue = queueCache.getCurrentQueue();
            Integer position = currentQueue.indexOf(getCurrentItem());
            if (position == null) {
                return 0;
            }
            position--;
            if (position < 0) {
                position = currentQueue.size() - 1;
            }
            setCurrentItem(currentQueue.get(position));
            return position;
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable skipToPosition(int position) {
        return Completable.fromAction(() -> {
            IndexedList<PlayQueueItem> currentQueue = queueCache.getCurrentQueue();
            setCurrentItem(currentQueue.get(position));
        }).subscribeOn(scheduler);
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

    @Nonnull
    private PlayQueueEvent getSavedQueueEvent() {
        if (currentCompositionSubject.getValue() != null) {
            return currentCompositionSubject.getValue();
        }
        return new PlayQueueEvent(getSavedQueueItem(), uiStatePreferences.getTrackPosition());
    }

    @Nullable
    private PlayQueueItem getSavedQueueItem() {
        long id = uiStatePreferences.getCurrentPlayQueueId();
        PlayQueueCompositionDto entity = playQueueDao.getPlayQueueItem(id);
        if (entity == null) {
            return null;
        }
        return toPlayQueueItem(entity);
    }

    private void setCurrentItem(@Nullable PlayQueueItem item) {
        long itemId = NO_COMPOSITION;
        long compositionId = NO_COMPOSITION;
        if (item != null) {
            itemId = item.getId();
            compositionId = item.getComposition().getId();
        }
        uiStatePreferences.setCurrentPlayQueueItemId(itemId);
        uiStatePreferences.setCurrentCompositionId(compositionId);
        currentCompositionSubject.onNext(new PlayQueueEvent(item));
    }

    @Nullable
    private PlayQueueItem getCurrentItem() {
        if (currentCompositionSubject.getValue() != null) {
            return currentCompositionSubject.getValue().getPlayQueueItem();
        }
        return getSavedQueueItem();
    }

    private void selectCurrentItem(PlayQueueLists queueLists, int startPosition) {
        PlayQueueItem item;
        if (startPosition == NO_POSITION) {
            List<PlayQueueItem> currentQueue = settingsPreferences.isRandomPlayingEnabled()?
                    queueLists.getShuffledQueue() : queueLists.getQueue();
            item = currentQueue.get(0);
        } else {
            item = queueLists.getQueue().get(startPosition);
        }
        setCurrentItem(item);
    }

    private List<PlayQueueItem> toSortedQueue(boolean isRandom, List<PlayQueueCompositionDto> items) {
        Collections.sort(items, (first, second) -> {
            if (isRandom) {
                return Integer.compare(first.getShuffledPosition(), second.getShuffledPosition());
            } else {
                return Integer.compare(first.getPosition(), second.getPosition());
            }
        });
        return mapList(items, this::toPlayQueueItem);
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
            Integer currentPosition = newQueue.indexOf(currentItem);
            PlayQueueItem newItem = newQueue.get(currentPosition);
            if (!PlayQueueItemHelper.areSourcesTheSame(newItem, currentItem)) {
                currentCompositionSubject.onNext(new PlayQueueEvent(newItem, true));
            }
        } else {
            //item not found, select new item on this position
            Integer currentPosition = currentQueue.indexOf(currentItem);
            if (currentPosition != null) {
                selectItemAt(newQueue, currentPosition);
            }
        }
    }
}
