package com.github.anrimian.musicplayer.data.repositories.play_queue;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueCompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.utils.collections.IndexedList;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
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

    @Nullable
    private IndexedList<PlayQueueItem> currentQueue;

    public PlayQueueRepositoryImpl(PlayQueueDaoWrapper playQueueDao,
                                   SettingsRepository settingsPreferences,
                                   UiStatePreferences uiStatePreferences,
                                   Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.settingsPreferences = settingsPreferences;
        this.uiStatePreferences = uiStatePreferences;
        this.scheduler = scheduler;
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
    public Integer getCompositionPosition(@Nonnull PlayQueueItem playQueueItem) {
        if (currentQueue == null) {
            return null;
        }
        return currentQueue.indexOf(playQueueItem);
    }

    @Override
    public Observable<PlayQueueEvent> getCurrentQueueItemObservable() {
        return withDefaultValue(currentCompositionSubject, this::getSavedQueueEvent)
                .subscribeOn(scheduler);
    }

    @Override
    public Flowable<List<PlayQueueItem>> getPlayQueueObservable() {
        return Flowable.combineLatest(settingsPreferences.getRandomPlayingObservable().toFlowable(BackpressureStrategy.BUFFER),
                playQueueDao.getPlayQueueObservable(),
                this::toSortedQueue)
                .doOnNext(list -> {
                    IndexedList<PlayQueueItem> newQueue = new IndexedList<>(list);
                    checkForCurrentItemInNewQueue(newQueue);
                    this.currentQueue = newQueue;
                });
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        Completable.fromAction(() -> {
            if (enabled) {
                PlayQueueItem item = getCurrentItem();
                playQueueDao.reshuffleQueue(item);
            }
            settingsPreferences.setRandomPlayingEnabled(enabled);
        }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Single<Integer> skipToNext() {
        return Single.fromCallable(() -> {
            if (currentQueue == null) {
                return 0;
            }
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
        });
    }

    @Override
    public Single<Integer> skipToPrevious() {
        return Single.fromCallable(() -> {
            if (currentQueue == null) {
                return 0;
            }
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
        });
    }

    @Override
    public Completable skipToPosition(int position) {
        return Completable.fromAction(() -> {
            if (currentQueue == null) {
                return;
            }
            setCurrentItem(currentQueue.get(position));
        });
    }

    @Override
    public Completable removeQueueItem(PlayQueueItem item) {
        return Completable.fromAction(() -> {
            if (currentQueue == null) {
                return;
            }
            Integer currentPosition = null;
            PlayQueueItem currentItem = getCurrentItem();
            if (item.equals(currentItem)) {
                currentPosition = currentQueue.indexOf(currentItem);
            }
            playQueueDao.deleteItem(item.getId());

            if (currentPosition != null) {
                selectItemAt(currentQueue, currentPosition + 1);
            }
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
            if (currentQueue == null) {
                return;
            }
            PlayQueueItem currentItem = getCurrentItem();
            playQueueDao.addCompositionsToQueue(compositions, currentItem);
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToEnd(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            if (currentQueue == null) {
                return;
            }
            playQueueDao.addCompositionsToEndQueue(compositions);
        }).subscribeOn(scheduler);
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
        PlayQueueCompositionEntity entity = playQueueDao.getPlayQueueItem(id);
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

    private List<PlayQueueItem> toSortedQueue(boolean isRandom, List<PlayQueueCompositionEntity> items) {
        Collections.sort(items, (first, second) -> {
            if (isRandom) {
                return Integer.compare(first.getShuffledPosition(), second.getShuffledPosition());
            } else {
                return Integer.compare(first.getPosition(), second.getPosition());
            }
        });
        return mapList(items, this::toPlayQueueItem);
    }

    private PlayQueueItem toPlayQueueItem(PlayQueueCompositionEntity entity) {
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
        if (this.currentQueue != null) {
            PlayQueueItem currentItem = getCurrentItem();
            if (!newQueue.contains(currentItem)) {
                Integer currentPosition = this.currentQueue.indexOf(currentItem);
                if (currentPosition != null) {
                    selectItemAt(newQueue, currentPosition);
                }
            }
        }
    }
}
