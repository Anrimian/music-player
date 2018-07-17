package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import android.support.annotation.NonNull;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDaoWrapper;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.simplemusicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static io.reactivex.subjects.BehaviorSubject.create;

/**
 * Created on 16.04.2018.
 */
public class PlayQueueRepositoryImpl implements PlayQueueRepository{

    private final PlayQueueDaoWrapper playQueueDao;
    private final StorageMusicDataSource storageMusicDataSource;
    private final SettingsPreferences settingsPreferences;
    private final UiStatePreferences uiStatePreferences;
    private final Scheduler scheduler;

    private final BehaviorSubject<List<Composition>> playQueueSubject = create();
    private final BehaviorSubject<CompositionEvent> currentCompositionSubject = create();

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
        return Single.fromCallable(() -> new PlayQueue(compositions, settingsPreferences.isRandomPlayingEnabled()))
                .doOnSuccess(this::savePlayQueue)
                .map(this::getSelectedPlayQueue)
                .doOnSuccess(playQueue -> {
                    setCurrentComposition(playQueue.get(0));
                    playQueueSubject.onNext(playQueue);
                })
                .toCompletable()
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<CompositionEvent> getCurrentCompositionObservable() {
        return withDefaultValue(currentCompositionSubject, this::getCompositionEvent)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Composition>> getPlayQueueObservable() {
        return withDefaultValue(playQueueSubject, getPlayQueue())
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Integer> skipToNext() {
        return getSavedPlayQueue()
                .map(playQueue -> {
                    List<Composition> compositions = getSelectedPlayQueue(playQueue);
                    int position = playQueue.getPosition(getCurrentComposition());
                    if (position >= compositions.size() - 1) {
                        position = 0;
                    } else {
                        position++;
                    }
                    setCurrentComposition(compositions.get(position));
                    return position;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Integer> skipToPrevious() {
        return getSavedPlayQueue()
                .map(playQueue -> {
                    List<Composition> compositions = getSelectedPlayQueue(playQueue);
                    int position = getCurrentPosition(getCurrentComposition());
                    position--;
                    if (position < 0) {
                        position = compositions.size() - 1;
                    }
                    setCurrentComposition(compositions.get(position));
                    return position;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable skipToPosition(int position) {
        return getSavedPlayQueue()
                .map(this::getSelectedPlayQueue)
                .doOnSuccess(list -> setCurrentComposition(list.get(position)))
                .toCompletable()
                .subscribeOn(scheduler);
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        getSavedPlayQueue().doOnSuccess(playQueue -> {
            Composition currentComposition = getCompositionEvent().getComposition();
            settingsPreferences.setRandomPlayingEnabled(enabled);
            playQueue.changeShuffleMode(enabled);
            if (enabled) {
                playQueue.moveCompositionToTopInShuffledList(currentComposition);
                playQueueDao.setShuffledPlayQueue(playQueue.getShuffledQueue());
            }
            playQueueSubject.onNext(getSelectedPlayQueue(playQueue));
        }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public int getCompositionPosition(@NonNull Composition composition) {
        return playQueue.getPosition(composition);
    }

    private Single<List<Composition>> getPlayQueue() {
        return getSavedPlayQueue()
                .map(this::getSelectedPlayQueue);
    }

    private void setCurrentComposition(@Nullable Composition composition) {
        long id = composition == null? NO_COMPOSITION: composition.getId();
        uiStatePreferences.setCurrentCompositionId(id);
        currentCompositionSubject.onNext(new CompositionEvent(composition));
    }

    private List<Composition> getSelectedPlayQueue(PlayQueue playQueue) {
        return playQueue.getCurrentPlayQueue();
    }

    private int getCurrentPosition(Composition composition) {
        return playQueue.getPosition(composition);
    }

    private Single<PlayQueue> getSavedPlayQueue() {
        return Single.fromCallable(() -> {
            if (playQueue == null) {
                synchronized (this) {
                    if (playQueue == null) {
                        playQueue = loadPlayQueue();
                        subscribeOnCompositionChanges();
                    }
                }
            }
            return playQueue;
        });
    }

    private void subscribeOnCompositionChanges() {
        if (changeDisposable == null && !playQueue.isEmpty()) {
            changeDisposable = storageMusicDataSource.getChangeObservable()
                    .subscribeOn(scheduler)
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

    private void processDeleteChange(List<Composition> changedCompositions) {
        List<Composition> compositionsToDelete = new ArrayList<>();
        Integer currentCompositionPosition = playQueue.getPosition(getCurrentComposition());
        boolean currentCompositionDeleted = false;

        for (Composition deletedComposition : changedCompositions) {
            long id = deletedComposition.getId();
            if (playQueue.getCompositionById(id) != null) {
                if (deletedComposition.equals(getCurrentComposition())) {
                    currentCompositionDeleted = true;
                }
                compositionsToDelete.add(deletedComposition);
            }
        }
        playQueue.deleteCompositions(compositionsToDelete);
        if (!compositionsToDelete.isEmpty()) {

            //optimize later if need
            playQueueDao.setShuffledPlayQueue(playQueue.getShuffledQueue());
            playQueueDao.setPlayQueue(playQueue.getCompositionQueue());

            playQueueSubject.onNext(playQueue.getCurrentPlayQueue());

            if (currentCompositionDeleted) {
                List<Composition> compositions = getSelectedPlayQueue(playQueue);
                Composition newComposition = null;
                if (!compositions.isEmpty()) {
                    if (currentCompositionPosition >= compositions.size()) {
                        currentCompositionPosition = 0;
                    }
                    newComposition = compositions.get(currentCompositionPosition);
                }
                currentCompositionSubject.onNext(new CompositionEvent(newComposition));
            }
        }
    }

    private void processModifyChange(List<Composition> changedCompositions) {
        List<Composition> compositionsToNotify = new ArrayList<>();
        Composition changedCurrentComposition = null;

        for (Composition modifiedComposition : changedCompositions) {
            long id = modifiedComposition.getId();
            if (playQueue.getCompositionById(id) != null) {
                if (modifiedComposition.equals(getCurrentComposition())) {
                    changedCurrentComposition = modifiedComposition;
                }

                playQueue.updateComposition(modifiedComposition);
                compositionsToNotify.add(modifiedComposition);
            }
        }
        if (!compositionsToNotify.isEmpty()) {
            playQueueSubject.onNext(playQueue.getCurrentPlayQueue());

            if (changedCurrentComposition != null) {
                currentCompositionSubject.onNext(new CompositionEvent(changedCurrentComposition));
            }
        }
    }

    private void savePlayQueue(PlayQueue playQueue) {
        playQueueDao.setShuffledPlayQueue(playQueue.getShuffledQueue());
        playQueueDao.setPlayQueue(playQueue.getCompositionQueue());

        this.playQueue = playQueue;

        subscribeOnCompositionChanges();
    }

    @SuppressWarnings("unchecked")
    private PlayQueue loadPlayQueue() {
        Map<Long, Composition> allCompositionMap = storageMusicDataSource.getCompositionsMap();
        List<Composition> playQueue = playQueueDao.getPlayQueue(allCompositionMap);
        List<Composition> shuffledQueue = playQueueDao.getShuffledPlayQueue(allCompositionMap);
        return new PlayQueue(playQueue, shuffledQueue, settingsPreferences.isRandomPlayingEnabled());
    }

    @Nonnull
    private CompositionEvent getCompositionEvent() {
        if (currentCompositionSubject.getValue() != null) {
            return currentCompositionSubject.getValue();
        }

        return new CompositionEvent(getSavedComposition());
    }

    private Composition getCurrentComposition() {
        if (currentCompositionSubject.getValue() != null) {
            return currentCompositionSubject.getValue().getComposition();
        }
        return getSavedComposition();
    }

    @Nullable
    private Composition getSavedComposition() {
        long id = uiStatePreferences.getCurrentCompositionId();
        return storageMusicDataSource.getCompositionById(id);
    }
}
