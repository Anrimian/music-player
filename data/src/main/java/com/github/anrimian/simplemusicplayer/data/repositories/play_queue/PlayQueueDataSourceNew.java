package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableMap;

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
public class PlayQueueDataSourceNew {

    private final PlayQueueDao playQueueDao;
    private final StorageMusicDataSource storageMusicDataSource;
    private final SettingsPreferences settingsPreferences;
    private final Scheduler scheduler;

    @Nullable
    private PlayQueue playQueue;

    public PlayQueueDataSourceNew(PlayQueueDao playQueueDao,
                                  StorageMusicDataSource storageMusicDataSource,
                                  SettingsPreferences settingsPreferences,
                                  Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.storageMusicDataSource = storageMusicDataSource;
        this.settingsPreferences = settingsPreferences;
        this.scheduler = scheduler;
    }

    public Completable setPlayQueue(List<Composition> compositions) {
        return Single.fromCallable(() -> new PlayQueue(compositions))
                .flatMapCompletable(this::savePlayQueue)
                .subscribeOn(scheduler);
    }

    public Single<List<Composition>> getPlayQueue() {
        return getSavedPlayQueue()
                .map(playQueue -> {
                    if (settingsPreferences.isRandomPlayingEnabled()) {
                        return playQueue.getShuffledPlayList();
                    }
                    return playQueue.getInitialPlayList();
                }).subscribeOn(scheduler);
    }

    /**
     *
     * @return new position of current composition
     */
    public int setRandomPlayingEnabled(boolean enabled, Composition currentComposition) {
        if (playQueue == null) {
            throw new IllegalStateException("change play mode before initialization");
        }

        settingsPreferences.setRandomPlayingEnabled(enabled);
        return 0;
//        if (enabled) {
//            shuffledPlayList.remove(currentComposition);
//            shuffledPlayList.add(0, currentComposition);
////            savePlayQueue();//TODO optimize in next refactoring wave
//            return 0;
//        }
//        return initialPlayList.indexOf(currentComposition);
    }

    private Single<PlayQueue> getSavedPlayQueue() {
        return Single.fromCallable(() -> {
            if (playQueue == null) {
                playQueue = loadPlayQueue();
            }
            return playQueue;
        });
    }

    private Completable savePlayQueue(PlayQueue playQueue) {
        return Completable.fromRunnable(() -> {
            playQueueDao.deletePlayQueue();

            List<PlayQueueEntity> playQueueEntityList = new ArrayList<>();

            for (Composition composition: playQueue.getCompositionMap().values()) {
                PlayQueueEntity playQueueEntity = new PlayQueueEntity();
                playQueueEntity.setId(composition.getId());
                playQueueEntity.setPosition(playQueue.getPosition(composition));
                playQueueEntity.setShuffledPosition(playQueue.getShuffledPosition(composition));

                playQueueEntityList.add(playQueueEntity);
            }

            playQueueDao.setPlayQueue(playQueueEntityList);

            this.playQueue = playQueue;
        });
    }

    @SuppressWarnings("unchecked")
    private PlayQueue loadPlayQueue() {
        Map<Long, Composition> allCompositionMap = storageMusicDataSource.getCompositionsList().getHashMap();
        List<PlayQueueEntity> playQueueEntities = playQueueDao.getPlayQueue();

        Map<Long, Integer> initialPlayList = new HashMap<>(playQueueEntities.size());
        Map<Long, Integer> shuffledPlayList = new HashMap<>(playQueueEntities.size());
        Map<Long, Composition> compositionMap = new HashMap<>(playQueueEntities.size());
        for (PlayQueueEntity playQueueEntity: playQueueEntities) {
            Composition composition = allCompositionMap.get(playQueueEntity.getId());
            if (composition == null) {
                //TODO delete
            } else {
                long id = composition.getId();
                compositionMap.put(id, composition);
                initialPlayList.put(id, playQueueEntity.getPosition());
                shuffledPlayList.put(id, playQueueEntity.getShuffledPosition());
            }
        }
        return new PlayQueue(initialPlayList, shuffledPlayList, compositionMap);
    }
}
