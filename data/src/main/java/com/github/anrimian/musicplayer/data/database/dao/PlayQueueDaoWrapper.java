package com.github.anrimian.musicplayer.data.database.dao;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.ShuffledPlayQueueEntity;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.utils.java.Function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public void setShuffledPlayQueueItems(List<PlayQueueItem> shuffledQueue) {
        appDatabase.runInTransaction(() -> {
            playQueueDao.deleteShuffledPlayQueue();
            playQueueDao.insertShuffledPlayQueue(toShuffledEntityItemsList(shuffledQueue));
        });
    }

    public void setPlayQueue(List<Composition> compositionQueue) {
        appDatabase.runInTransaction(() -> {
            playQueueDao.deletePlayQueue();
            playQueueDao.insertPlayQueue(toEntityList(compositionQueue));
        });
    }

    public void setPlayQueueItems(List<PlayQueueItem> compositionQueue) {
        appDatabase.runInTransaction(() -> {
            playQueueDao.deletePlayQueue();
            playQueueDao.insertPlayQueue(toEntityItemsList(compositionQueue));
        });
    }

    public List<PlayQueueItem> setShuffledPlayQueueNew(List<Composition> compositionQueue) {
        return appDatabase.runInTransaction(() -> {
            playQueueDao.deleteShuffledPlayQueue();
            long[] ids = playQueueDao.insertShuffledPlayQueue(toShuffledEntityList(compositionQueue));
            return createPlayQueueItems(compositionQueue, ids);
        });
    }

    public List<PlayQueueItem> setPlayQueueNew(List<Composition> compositionQueue) {
        return appDatabase.runInTransaction(() -> {
            playQueueDao.deletePlayQueue();
            long[] ids = playQueueDao.insertPlayQueue(toEntityList(compositionQueue));
            return createPlayQueueItems(compositionQueue, ids);
        });
    }

    public List<PlayQueueItem> getPlayQueue(
            Function<Map<Long, Composition>> compositionsCallable) {
        return toPlayQueueItem(playQueueDao.getPlayQueue(), compositionsCallable.call());
    }

    public List<PlayQueueItem> getShuffledPlayQueue(
            Function<Map<Long, Composition>> compositions) {
        return toShuffledPlayQueueItem(playQueueDao.getShuffledPlayQueue(), compositions.call());
    }

    private List<PlayQueueItem> toShuffledPlayQueueItem(List<ShuffledPlayQueueEntity> entities,
                                                        Map<Long, Composition> compositionMap) {
        Iterator<ShuffledPlayQueueEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            ShuffledPlayQueueEntity playQueueEntity = iterator.next();
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(playQueueEntity.getAudioId());
            if (composition == null) {
                playQueueDao.deleteShuffledItem(id);
                iterator.remove();
            }
        }
        List<PlayQueueItem> queueItems = new ArrayList<>();
        for (ShuffledPlayQueueEntity playQueueEntity: entities) {
            Composition composition = compositionMap.get(playQueueEntity.getAudioId());
            queueItems.add(new PlayQueueItem(playQueueEntity.getId(), composition));
        }
        return queueItems;
    }

    private List<PlayQueueItem> toPlayQueueItem(List<PlayQueueEntity> entities,
                                                Map<Long, Composition> compositionMap) {
        Iterator<PlayQueueEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            PlayQueueEntity playQueueEntity = iterator.next();
            long id = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(playQueueEntity.getAudioId());
            if (composition == null) {
                playQueueDao.deleteItem(id);
                iterator.remove();
            }
        }
        List<PlayQueueItem> queueItems = new ArrayList<>();
        for (PlayQueueEntity playQueueEntity: entities) {
            Composition composition = compositionMap.get(playQueueEntity.getAudioId());
            queueItems.add(new PlayQueueItem(playQueueEntity.getId(), composition));
        }
        return queueItems;
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

    private List<ShuffledPlayQueueEntity> toShuffledEntityItemsList(List<PlayQueueItem> playQueueItems) {
        List<ShuffledPlayQueueEntity> entityList = new ArrayList<>();

        for (int i = 0; i < playQueueItems.size(); i++) {
            PlayQueueItem item = playQueueItems.get(i);
            ShuffledPlayQueueEntity playQueueEntity = new ShuffledPlayQueueEntity();

            Composition composition = item.getComposition();
            playQueueEntity.setAudioId(composition.getId());

            playQueueEntity.setId(item.getId());
            playQueueEntity.setPosition(i);

            entityList.add(playQueueEntity);
        }
        return entityList;
    }

    private List<PlayQueueEntity> toEntityItemsList(List<PlayQueueItem> playQueueItems) {
        List<PlayQueueEntity> entityList = new ArrayList<>();

        for (int i = 0; i < playQueueItems.size(); i++) {
            PlayQueueItem item = playQueueItems.get(i);
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();

            Composition composition = item.getComposition();
            playQueueEntity.setAudioId(composition.getId());

            playQueueEntity.setId(item.getId());
            playQueueEntity.setPosition(i);

            entityList.add(playQueueEntity);
        }
        return entityList;
    }

    private List<PlayQueueItem> createPlayQueueItems(List<Composition> compositions, long[] ids) {
        List<PlayQueueItem> items = new ArrayList<>();

        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            PlayQueueItem playQueueItem = new PlayQueueItem(ids[i], composition);
            items.add(playQueueItem);
        }
        return items;
    }
}
