package com.github.anrimian.musicplayer.data.database.dao;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.ShuffledPlayQueueEntity;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * Created on 02.07.2018.
 */
public class PlayQueueDaoWrapper {

    private final AppDatabase appDatabase;
    private final PlayQueueDao playQueueDao;

    public PlayQueueDaoWrapper(AppDatabase appDatabase, PlayQueueDao playQueueDao) {
        this.appDatabase = appDatabase;
        this.playQueueDao = playQueueDao;
    }

    public void setShuffledPlayQueue(List<Composition> shuffledQueue) {
        appDatabase.runInTransaction(() -> {
            playQueueDao.deleteShuffledPlayQueue();
            playQueueDao.insertShuffledPlayQueue(toShuffledEntityList(shuffledQueue));
        });
    }

    public void setPlayQueue(List<Composition> compositionQueue) {
        appDatabase.runInTransaction(() -> {
            playQueueDao.deletePlayQueue();
            playQueueDao.insertPlayQueue(toEntityList(compositionQueue));
        });
    }

    public Flowable<List<PlayQueueItem>> getPlayQueueObservable(
            Callable<Map<Long, Composition>> compositionsCallable) {
        return playQueueDao.getPlayQueueObservable()
                .map(entities -> toPlayQueueItem(entities, compositionsCallable.call()));
    }

    public Flowable<List<PlayQueueItem>> getShuffledPlayQueueObservable(
            Callable<Map<Long, Composition>> compositionsCallable) {
        return playQueueDao.getShuffledPlayQueueObservable()
                .map(entities -> toShuffledPlayQueueItem(entities, compositionsCallable.call()));
    }

    private List<PlayQueueItem> toShuffledPlayQueueItem(List<ShuffledPlayQueueEntity> entities,
                                                        Map<Long, Composition> compositionMap) {
        Iterator<ShuffledPlayQueueEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            ShuffledPlayQueueEntity playQueueEntity = iterator.next();
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(id);
            if (composition == null) {
                playQueueDao.deleteComposition(id);
                iterator.remove();
            }
        }
        List<PlayQueueItem> queueItems = new ArrayList<>();
        for (ShuffledPlayQueueEntity playQueueEntity: entities) {
            long id = playQueueEntity.getId();
            Composition composition = compositionMap.get(id);
            queueItems.add(new PlayQueueItem(id, composition));
        }
        return queueItems;
    }

    private List<PlayQueueItem> toPlayQueueItem(List<PlayQueueEntity> entities,
                                                Map<Long, Composition> compositionMap) {
        Iterator<PlayQueueEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            PlayQueueEntity playQueueEntity = iterator.next();
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(id);
            if (composition == null) {
                playQueueDao.deleteComposition(id);
                iterator.remove();
            }
        }
        List<PlayQueueItem> queueItems = new ArrayList<>();
        for (PlayQueueEntity playQueueEntity: entities) {
            long id = playQueueEntity.getId();
            Composition composition = compositionMap.get(id);
            queueItems.add(new PlayQueueItem(id, composition));
        }
        return queueItems;
    }

    public List<Composition> getPlayQueue(Map<Long, Composition> compositionMap) {
        List<PlayQueueEntity> playQueueEntities = playQueueDao.getPlayQueue();

        Iterator<PlayQueueEntity> iterator = playQueueEntities.iterator();
        while (iterator.hasNext()) {
            PlayQueueEntity playQueueEntity = iterator.next();
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(id);
            if (composition == null) {
                playQueueDao.deleteComposition(id);
                iterator.remove();
            }
        }

        List<Composition> compositions = new ArrayList<>();
        for (PlayQueueEntity playQueueEntity: playQueueEntities) {
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(id);
            compositions.add(composition);
        }
        return compositions;
    }

    public List<Composition> getShuffledPlayQueue(Map<Long, Composition> compositionMap) {
        List<ShuffledPlayQueueEntity> playQueueEntities = playQueueDao.getShuffledPlayQueue();

        Iterator<ShuffledPlayQueueEntity> iterator = playQueueEntities.iterator();
        while (iterator.hasNext()) {
            ShuffledPlayQueueEntity playQueueEntity = iterator.next();
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(id);
            if (composition == null) {
                playQueueDao.deleteShuffledComposition(id);
                iterator.remove();
            }
        }

        List<Composition> compositions = new ArrayList<>();
        for (ShuffledPlayQueueEntity playQueueEntity: playQueueEntities) {
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(id);
            compositions.add(composition);
        }
        return compositions;
    }

    private List<ShuffledPlayQueueEntity> toShuffledEntityList(List<Composition> shuffledQueue) {
        List<ShuffledPlayQueueEntity> entityList = new ArrayList<>();

        for (int i = 0; i < shuffledQueue.size(); i++) {
            Composition composition = shuffledQueue.get(i);
            ShuffledPlayQueueEntity playQueueEntity = new ShuffledPlayQueueEntity();
            playQueueEntity.setAudioId(composition.getId());
            playQueueEntity.setPosition(i);

            entityList.add(playQueueEntity);
        }
        return entityList;
    }

    private List<PlayQueueEntity> toEntityList(List<Composition> shuffledQueue) {
        List<PlayQueueEntity> entityList = new ArrayList<>();

        for (int i = 0; i < shuffledQueue.size(); i++) {
            Composition composition = shuffledQueue.get(i);
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();
            playQueueEntity.setAudioId(composition.getId());
            playQueueEntity.setPosition(i);

            entityList.add(playQueueEntity);
        }
        return entityList;
    }
}
