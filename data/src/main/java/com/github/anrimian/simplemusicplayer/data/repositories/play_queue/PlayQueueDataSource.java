package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDaoWrapper;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static io.reactivex.subjects.BehaviorSubject.create;

/**
 * Created on 16.04.2018.
 */
public class PlayQueueDataSource {

    private final PlayQueueDaoWrapper playQueueDao;
    private final StorageMusicDataSource storageMusicDataSource;
    private final SettingsPreferences settingsPreferences;

    private final BehaviorSubject<List<Composition>> playQueueSubject = create();
    private final PublishSubject<Change<List<Composition>>> changeSubject = PublishSubject.create();

    @Nullable
    private PlayQueue playQueue;

    private Disposable changeDisposable;

    public PlayQueueDataSource(PlayQueueDaoWrapper playQueueDao,
                               StorageMusicDataSource storageMusicDataSource,
                               SettingsPreferences settingsPreferences) {
        this.playQueueDao = playQueueDao;
        this.storageMusicDataSource = storageMusicDataSource;
        this.settingsPreferences = settingsPreferences;
    }

    public Single<List<Composition>> setPlayQueue(List<Composition> compositions) {
        return Single.fromCallable(() -> new PlayQueue(compositions, settingsPreferences.isRandomPlayingEnabled()))
                .doOnSuccess(this::savePlayQueue)
                .map(this::getSelectedPlayQueue)
                .doOnSuccess(playQueueSubject::onNext);
    }

    public Single<List<Composition>> getPlayQueue() {
        return getSavedPlayQueue()
                .map(this::getSelectedPlayQueue);
    }

    public Observable<List<Composition>> getPlayQueueObservable() {
        return withDefaultValue(playQueueSubject, getPlayQueue());
    }

    public Flowable<Change<List<Composition>>> getChangeObservable() {
        return changeSubject.toFlowable(BackpressureStrategy.BUFFER);
    }

    /**
     *
     * @return new position of current composition
     */
    public Single<Integer> setRandomPlayingEnabled(boolean enabled, Composition currentComposition) {
        return getSavedPlayQueue()
                .flatMapCompletable(playQueue -> Completable.fromRunnable(() -> {
                    settingsPreferences.setRandomPlayingEnabled(enabled);
                    playQueue.changeShuffleMode(enabled);
                    if (enabled) {
                        playQueue.moveCompositionToTopInShuffledList(currentComposition);
                        playQueueDao.setShuffledPlayQueue(playQueue.getShuffledQueue());
                    }
                    playQueueSubject.onNext(getSelectedPlayQueue(playQueue));
                }))
                .andThen(Single.fromCallable(() -> getCurrentPosition(currentComposition)));
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
        List<Composition> compositionsToNotify = new ArrayList<>();
        for (Composition deletedComposition : changedCompositions) {
            long id = deletedComposition.getId();
            if (playQueue.getCompositionById(id) != null) {
                playQueue.deleteComposition(id);
                compositionsToNotify.add(deletedComposition);
            }
        }
        if (!compositionsToNotify.isEmpty()) {

            //optimize later if need
            playQueueDao.setShuffledPlayQueue(playQueue.getShuffledQueue());
            playQueueDao.setPlayQueue(playQueue.getCompositionQueue());

            changeSubject.onNext(new Change<>(ChangeType.DELETED, compositionsToNotify));
            updateSubject();
        }
    }

    private void processModifyChange(List<Composition> changedCompositions) {
        List<Composition> compositionsToNotify = new ArrayList<>();
        for (Composition modifiedComposition : changedCompositions) {
            long id = modifiedComposition.getId();
            if (playQueue.getCompositionById(id) != null) {
                playQueue.updateComposition(modifiedComposition);
                compositionsToNotify.add(modifiedComposition);
            }
        }
        if (!compositionsToNotify.isEmpty()) {
            changeSubject.onNext(new Change<>(ChangeType.MODIFY, compositionsToNotify));
            updateSubject();
        }
    }

    private void updateSubject() {
        List<Composition> cachedCompositions = playQueueSubject.getValue();
        cachedCompositions.clear();
        cachedCompositions.addAll(playQueue.getCurrentPlayQueue());
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
}
