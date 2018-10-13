package com.github.anrimian.musicplayer.data.repositories.play_queue;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static io.reactivex.subjects.BehaviorSubject.create;

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private final PlayQueueDaoWrapper playQueueDao;
    private final StorageMusicDataSource storageMusicDataSource;
    private final SettingsPreferences settingsPreferences;
    private final UiStatePreferences uiStatePreferences;
    private final Scheduler scheduler;

    private final BehaviorSubject<List<PlayQueueItem>> playQueueSubject = create();
    private final BehaviorSubject<PlayQueueEvent> currentCompositionSubject = create();

    @Nullable
    private PlayQueue playQueue;

    private Disposable changeDisposable;

    public PlayQueueRepositoryImpl(PlayQueueDaoWrapper playQueueDao,
                                   StorageMusicDataSource storageMusicDataSource,
                                   SettingsPreferences settingsPreferences,
                                   UiStatePreferences uiStatePreferences,
                                   Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.storageMusicDataSource = storageMusicDataSource;
        this.settingsPreferences = settingsPreferences;
        this.uiStatePreferences = uiStatePreferences;
        this.scheduler = scheduler;
    }

    @Override
    public Completable setPlayQueue(List<Composition> compositions) {
        if (compositions.isEmpty()) {
            return Completable.complete();
        }
        return Single.fromCallable(() -> createPlayQueue(compositions))
                .doOnSuccess(playQueue -> {
                    this.playQueue = playQueue;
                    subscribeOnCompositionChanges();
                })
                .map(PlayQueue::getCurrentPlayQueue)
                .doOnSuccess(playQueue -> {
                    playQueueSubject.onNext(playQueue);
                    setCurrentItem(playQueue.get(0));
                })
                .toCompletable()
                .subscribeOn(scheduler);
    }

    @Nullable
    @Override
    public Integer getCompositionPosition(@Nonnull PlayQueueItem playQueueItem) {
        return getPlayQueue().getPosition(playQueueItem);
    }

    @Override
    public Observable<PlayQueueEvent> getCurrentQueueItemObservable() {
        return withDefaultValue(currentCompositionSubject, this::getSavedQueueEvent)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<PlayQueueItem>> getPlayQueueObservable() {
        return withDefaultValue(playQueueSubject, () -> getPlayQueue().getCurrentPlayQueue())
                .subscribeOn(scheduler);
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        Single.fromCallable(this::getPlayQueue)
                .doOnSuccess(playQueue -> {
                    PlayQueueItem item = getCurrentItem();
                    settingsPreferences.setRandomPlayingEnabled(enabled);
                    playQueue.changeShuffleMode(enabled);
                    if (enabled) {
                        playQueue.moveItemToTopInShuffledList(item);
                        playQueueDao.moveShuffledPositionToTop(item);
                    }
                    playQueueSubject.onNext(playQueue.getCurrentPlayQueue());
                }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Single<Integer> skipToNext() {
        return Single.fromCallable(this::getPlayQueue)
                .map(playQueue -> {
                    List<PlayQueueItem> items = playQueue.getCurrentPlayQueue();
                    Integer position = playQueue.getPosition(getCurrentItem());
                    if (position == null) {
                        return 0;
                    }
                    if (position >= items.size() - 1) {
                        position = 0;
                    } else {
                        position++;
                    }
                    setCurrentItem(items.get(position));
                    return position;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Integer> skipToPrevious() {
        return Single.fromCallable(this::getPlayQueue)
                .map(playQueue -> {
                    List<PlayQueueItem> compositions = playQueue.getCurrentPlayQueue();
                    Integer position = playQueue.getPosition(getCurrentItem());
                    if (position == null) {
                        return 0;
                    }
                    position--;
                    if (position < 0) {
                        position = compositions.size() - 1;
                    }
                    setCurrentItem(compositions.get(position));
                    return position;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable skipToPosition(int position) {
        return Single.fromCallable(this::getPlayQueue)
                .map(PlayQueue::getCurrentPlayQueue)
                .doOnSuccess(list -> setCurrentItem(list.get(position)))
                .toCompletable()
                .subscribeOn(scheduler);
    }

    private PlayQueue getPlayQueue() {
        if (playQueue == null) {
            synchronized (this) {
                if (playQueue == null) {
                    playQueue = loadPlayQueue();
                    if (!playQueue.isEmpty()) {
                        subscribeOnCompositionChanges();
                    }
                }
            }
        }
        return playQueue;
    }

    private void subscribeOnCompositionChanges() {
        if (changeDisposable == null) {
            changeDisposable = storageMusicDataSource.getChangeObservable()
                    .subscribe(this::processCompositionChange);
        }
    }

    private void processCompositionChange(Change<List<Composition>> change) {
        List<Composition> changedCompositions = change.getData();
        switch (change.getChangeType()) {
            case DELETED: {
                processDeleteChange(changedCompositions);
                break;
            }
            case MODIFY: {
                processModifyChange(changedCompositions);
                break;
            }
        }
    }

    private void processDeleteChange(List<Composition> deletedCompositions) {
        Integer currentPosition = getPlayQueue().getPosition(getCurrentItem());
        boolean currentItemDeleted = false;

        for (Composition deletedComposition : deletedCompositions) {
            PlayQueueItem currentItem = getCurrentItem();
            if (currentItem != null && currentItem.getComposition().equals(deletedComposition)) {
                currentItemDeleted = true;
            }
        }

        boolean updated = getPlayQueue().deleteCompositions(deletedCompositions);
        if (updated) {
            playQueueDao.deleteCompositionsFromQueue(deletedCompositions);
            playQueueSubject.onNext(getPlayQueue().getCurrentPlayQueue());

            if (currentItemDeleted) {
                List<PlayQueueItem> items = getPlayQueue().getCurrentPlayQueue();
                PlayQueueItem newItem = null;
                if (!items.isEmpty()) {
                    if (currentPosition == null || currentPosition >= items.size()) {
                        currentPosition = 0;
                    }
                    newItem = items.get(currentPosition);
                }
                setCurrentItem(newItem);
            }
        }
    }

    private void processModifyChange(List<Composition> changedCompositions) {
        boolean updatedCurrentComposition = false;
        boolean updated = false;

        for (Composition modifiedComposition : changedCompositions) {
            boolean updatedItem = getPlayQueue().updateComposition(modifiedComposition);
            if (!updated) {
                updated = updatedItem;
            }

            PlayQueueItem currentItem = getCurrentItem();
            if (currentItem != null && currentItem.getComposition().equals(modifiedComposition)) {
                currentItem.setComposition(modifiedComposition);
                updatedCurrentComposition = true;
            }
        }
        if (updated) {
            playQueueSubject.onNext(getPlayQueue().getCurrentPlayQueue());
        }
        if (updatedCurrentComposition) {
            currentCompositionSubject.onNext(new PlayQueueEvent(getCurrentItem()));
        }
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
        Composition composition = storageMusicDataSource.getCompositionById(uiStatePreferences.getCurrentCompositionId());
        if (composition == null) {
            return null;
        }
        return new PlayQueueItem(id, composition);
    }

    private PlayQueue loadPlayQueue() {
        PlayQueueLists lists = playQueueDao.getPlayQueue(storageMusicDataSource::getCompositionsMap);
        return new PlayQueue(
                lists.getQueue(),
                lists.getShuffledQueue(),
                settingsPreferences.isRandomPlayingEnabled());
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

    private PlayQueue createPlayQueue(List<Composition> compositions) {
        PlayQueueLists lists = playQueueDao.insertNewPlayQueue(compositions);
        return new PlayQueue(lists.getQueue(),
                lists.getShuffledQueue(),
                settingsPreferences.isRandomPlayingEnabled());
    }
}
