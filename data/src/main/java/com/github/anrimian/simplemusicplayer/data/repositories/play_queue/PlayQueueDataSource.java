package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
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

    private final PlayQueueDao playQueueDao;
    private final StorageMusicDataSource storageMusicDataSource;
    private final SettingsPreferences settingsPreferences;

    private final BehaviorSubject<List<Composition>> playQueueSubject = create();
    private final PublishSubject<Change<List<Composition>>> changeSubject = PublishSubject.create();

    @Nullable
    private PlayQueue playQueue;

    private Disposable changeDisposable;

    public PlayQueueDataSource(PlayQueueDao playQueueDao,
                               StorageMusicDataSource storageMusicDataSource,
                               SettingsPreferences settingsPreferences) {
        this.playQueueDao = playQueueDao;
        this.storageMusicDataSource = storageMusicDataSource;
        this.settingsPreferences = settingsPreferences;
    }

    public Single<List<Composition>> setPlayQueue(List<Composition> compositions) {
        return Single.fromCallable(() -> new PlayQueue(compositions))
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

    public Observable<Change<List<Composition>>> getChangeObservable() {
        return changeSubject;
    }

    /**
     *
     * @return new position of current composition
     */
    public Single<Integer> setRandomPlayingEnabled(boolean enabled, Composition currentComposition) {
        return getSavedPlayQueue()
                .flatMapCompletable(playQueue -> Completable.fromRunnable(() -> {
                    settingsPreferences.setRandomPlayingEnabled(enabled);
                    if (enabled) {
                        playQueue.shuffle();

                        playQueue.moveCompositionToTopInShuffledList(currentComposition);
                        playQueueDao.updatePlayQueue(toEntityList(playQueue));
                    }
                    playQueueSubject.onNext(getSelectedPlayQueue(playQueue));
                }))
                .andThen(Single.fromCallable(() -> getCurrentPosition(currentComposition)));
    }

    private List<Composition> getSelectedPlayQueue(PlayQueue playQueue) {
        if (settingsPreferences.isRandomPlayingEnabled()) {
            return playQueue.getShuffledPlayList();
        }
        return playQueue.getPlayList();
    }

    private int getCurrentPosition(Composition composition) {
        if (settingsPreferences.isRandomPlayingEnabled()) {
            return playQueue.getShuffledPosition(composition);
        }
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
                playQueueDao.deletePlayQueueEntity(id);

                Map<Long, Integer> shuffledPositionMap = playQueue.getShuffledPositionMap();
                int freeShuffledPosition = shuffledPositionMap.remove(id);
                for (Map.Entry<Long, Integer> entry : shuffledPositionMap.entrySet()) {// FIXME: 17.06.2018 concurrent modification
                    Long key = entry.getKey();
                    Integer position = entry.getValue();
                    if (position > freeShuffledPosition) {
                        int newPosition = position - 1;
                        shuffledPositionMap.put(key, newPosition);
                        playQueueDao.updateShuffledPosition(key, newPosition);
                    }
                }

                Map<Long, Integer> positionMap = playQueue.getPositionMap();
                int freeInitialPosition = positionMap.remove(id);
                for (Map.Entry<Long, Integer> entry : positionMap.entrySet()) {
                    Long key = entry.getKey();
                    Integer position = entry.getValue();
                    if (position > freeInitialPosition) {
                        int newPosition = position - 1;
                        positionMap.put(key, newPosition);
                        playQueueDao.updatePosition(key, newPosition);
                    }
                }

                compositionsToNotify.add(deletedComposition);
            }
        }
        if (!compositionsToNotify.isEmpty()) {
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
        cachedCompositions.addAll(getSelectedPlayQueue(playQueue));
    }

    private List<PlayQueueEntity> toEntityList(PlayQueue playQueue) {
        List<PlayQueueEntity> playQueueEntityList = new ArrayList<>();

        for (Composition composition: playQueue.getCompositionMap().values()) {
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();
            playQueueEntity.setId(composition.getId());
            playQueueEntity.setPosition(playQueue.getPosition(composition));
            playQueueEntity.setShuffledPosition(playQueue.getShuffledPosition(composition));

            playQueueEntityList.add(playQueueEntity);
        }
        return playQueueEntityList;
    }

    private void savePlayQueue(PlayQueue playQueue) {
        playQueueDao.deletePlayQueue();
        playQueueDao.setPlayQueue(toEntityList(playQueue));

        this.playQueue = playQueue;

        subscribeOnCompositionChanges();
    }

    @SuppressWarnings("unchecked")
    private PlayQueue loadPlayQueue() {
        Map<Long, Composition> allCompositionMap = storageMusicDataSource.getCompositionsMap();
        List<PlayQueueEntity> playQueueEntities = playQueueDao.getPlayQueue();

        Map<Long, Integer> initialPlayList = new HashMap<>(playQueueEntities.size());
        Map<Long, Integer> shuffledPlayList = new HashMap<>(playQueueEntities.size());
        Map<Long, Composition> compositionMap = new HashMap<>(playQueueEntities.size());
        for (PlayQueueEntity playQueueEntity: playQueueEntities) {
            long id = playQueueEntity.getId();
            Composition composition = allCompositionMap.get(id);
            if (composition == null) {
                playQueueDao.deletePlayQueueEntity(id);
            } else {
                compositionMap.put(id, composition);
                initialPlayList.put(id, playQueueEntity.getPosition());
                shuffledPlayList.put(id, playQueueEntity.getShuffledPosition());
            }
        }
        return new PlayQueue(initialPlayList, shuffledPlayList, compositionMap);
    }
}
