package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

/**
 * Created on 16.04.2018.
 */
public class PlayQueueDataSource {

    private final PlayQueueDao playQueueDao;
    private final StorageMusicDataSource storageMusicDataSource;
    private final SettingsPreferences settingsPreferences;
    private final Scheduler scheduler;

    @Nullable
    private PlayQueue playQueue;

    public PlayQueueDataSource(PlayQueueDao playQueueDao,
                               StorageMusicDataSource storageMusicDataSource,
                               SettingsPreferences settingsPreferences,
                               Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.storageMusicDataSource = storageMusicDataSource;
        this.settingsPreferences = settingsPreferences;
        this.scheduler = scheduler;
    }

    public Single<List<Composition>> setPlayQueue(List<Composition> compositions) {
        return Single.fromCallable(() -> new PlayQueue(compositions))
                .doOnSuccess(this::savePlayQueue)
                .map(this::getSelectedPlayQueue)
                .subscribeOn(scheduler);
    }

    public Single<List<Composition>> getPlayQueue() {
        return getSavedPlayQueue()
                .map(this::getSelectedPlayQueue)
                .subscribeOn(scheduler);
    }

    private List<Composition> getSelectedPlayQueue(PlayQueue playQueue) {
        if (settingsPreferences.isRandomPlayingEnabled()) {
            return playQueue.getShuffledPlayList();
        }
        return playQueue.getInitialPlayList();
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

                        moveCompositionToTop(currentComposition);
                        playQueueDao.updatePlayQueue(toEntityList(playQueue));
                    }
                }))
                .andThen(Single.fromCallable(() -> getCurrentPosition(currentComposition)))
                .subscribeOn(scheduler);
    }

    private void moveCompositionToTop(Composition composition) {
        Map<Long, Integer> shuffledPlayList = playQueue.getShuffledPlayMap();

        long id = composition.getId();
        for (Map.Entry<Long, Integer> entry: shuffledPlayList.entrySet()) {
            if (entry.getValue() == 0) {
                int previousPosition = shuffledPlayList.put(id, 0);
                shuffledPlayList.put(entry.getKey(), previousPosition);
                break;
            }
        }
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
                    }
                }
            }
            return playQueue;
        });
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
    }

    @SuppressWarnings("unchecked")
    private PlayQueue loadPlayQueue() {
        Map<Long, Composition> allCompositionMap = storageMusicDataSource.getCompositionsList().getHashMap();
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
