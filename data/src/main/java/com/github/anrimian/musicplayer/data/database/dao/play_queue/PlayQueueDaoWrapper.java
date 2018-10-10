package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntityNew;
import com.github.anrimian.musicplayer.data.database.entities.ShuffledPlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.utils.java.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

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

    @Deprecated
    public void setShuffledPlayQueueItems(List<PlayQueueItem> shuffledQueue) {
        appDatabase.runInTransaction(() -> {
            playQueueDao.deleteShuffledPlayQueue();
            playQueueDao.insertShuffledPlayQueue(toShuffledEntityItemsList(shuffledQueue));
        });
    }

    @Deprecated
    public void setPlayQueueItems(List<PlayQueueItem> compositionQueue) {
        appDatabase.runInTransaction(() -> {
            playQueueDao.deletePlayQueue();
            playQueueDao.insertPlayQueue(toEntityItemsList(compositionQueue));
        });
    }

    public void deleteCompositionsFromQueue(List<Composition> compositions) {
        appDatabase.runInTransaction(() -> {
            for (Composition composition: compositions) {
                playQueueDao.deleteComposition(composition.getId());
            }
        });
    }

    public void moveShuffledPositionToTop(PlayQueueItem item) {
        appDatabase.runInTransaction(() -> {
            long id = item.getId();
            int previousPosition = playQueueDao.getShuffledPosition(id);
            long firstItemId = playQueueDao.getQueueItemId(0);
            playQueueDao.updateShuffledPosition(id, 0);
            playQueueDao.updateShuffledPosition(firstItemId, previousPosition);
        });
    }

    @Deprecated
    public List<PlayQueueItem> setShuffledPlayQueueNew(List<Composition> compositionQueue) {
        return appDatabase.runInTransaction(() -> {
            playQueueDao.deleteShuffledPlayQueue();
            long[] ids = playQueueDao.insertShuffledPlayQueue(toShuffledEntityList(compositionQueue));
            return toPlayQueueItems(compositionQueue, ids);
        });
    }

    @Deprecated
    public List<PlayQueueItem> setPlayQueue(List<Composition> compositionQueue) {
        return appDatabase.runInTransaction(() -> {
            playQueueDao.deletePlayQueue();
            long[] ids = playQueueDao.insertPlayQueue(toEntityList(compositionQueue));
            return toPlayQueueItems(compositionQueue, ids);
        });
    }

    public PlayQueueLists insertNewPlayQueue(List<Composition> compositions) {
        return appDatabase.runInTransaction(() -> {
            List<Composition> shuffledList = new ArrayList<>(compositions);
            Random random = new Random();
            Collections.shuffle(shuffledList, random);
            return insertNewPlayQueue(compositions, shuffledList, random);
        });
    }

    @Deprecated
    public List<PlayQueueItem> getPlayQueue(
            Function<Map<Long, Composition>> compositionsCallable) {
        return toPlayQueueItem(playQueueDao.getPlayQueue(), compositionsCallable.call());
    }

    public PlayQueueLists getPlayQueueNew(Function<Map<Long, Composition>> compositionsCallable) {
        return toPlayQueueLists(playQueueDao.getPlayQueueNew(), compositionsCallable.call());
    }

    @Deprecated
    public List<PlayQueueItem> getShuffledPlayQueue(
            Function<Map<Long, Composition>> compositions) {
        return toShuffledPlayQueueItem(playQueueDao.getShuffledPlayQueue(), compositions.call());
    }

    PlayQueueLists insertNewPlayQueue(List<Composition> compositions,
                                      List<Composition> shuffledCompositions,
                                      Random rnd) {
        playQueueDao.deletePlayQueueNew();
        long[] ids = playQueueDao.insertPlayQueueNew(toEntityList(compositions, rnd));
        List<PlayQueueItem> items = toPlayQueueItems(compositions, ids);
        List<PlayQueueItem> shuffledItems = toPlayQueueItems(shuffledCompositions, ids);
        return new PlayQueueLists(items, shuffledItems);
    }

    @Deprecated
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

    private PlayQueueLists toPlayQueueLists(List<PlayQueueEntityNew> entities,
                                            Map<Long, Composition> compositions) {
        filterDeletedCompositions(entities, compositions);

        Collections.sort(entities, (first, second) ->
                Integer.compare(first.getPosition(), second.getPosition()));
        List<PlayQueueItem> items = toItems(entities, compositions);

        Collections.sort(entities, (first, second) ->
                Integer.compare(first.getShuffledPosition(), second.getShuffledPosition()));
        List<PlayQueueItem> shuffledItems = toItems(entities, compositions);

        return new PlayQueueLists(items, shuffledItems);
    }

    private List<PlayQueueItem> toItems(List<PlayQueueEntityNew> entities,
                                        Map<Long, Composition> compositionMap) {
        List<PlayQueueItem> items = new ArrayList<>();
        for (PlayQueueEntityNew entity: entities) {
            Composition composition = compositionMap.get(entity.getAudioId());
            items.add(new PlayQueueItem(entity.getId(), composition));
        }
        return items;
    }

    private void filterDeletedCompositions(List<PlayQueueEntityNew> entities,
                                           Map<Long, Composition> compositionMap) {
        Iterator<PlayQueueEntityNew> iterator = entities.iterator();
        while (iterator.hasNext()) {
            PlayQueueEntityNew playQueueEntity = iterator.next();
            long audioId = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(audioId);
            if (composition == null) {
                playQueueDao.deleteItemNew(audioId);
                iterator.remove();
            }
        }
    }

    @Deprecated
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

    private List<PlayQueueEntity> toEntityList(List<Composition> list) {
        List<PlayQueueEntity> entityList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Composition composition = list.get(i);
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();
            playQueueEntity.setAudioId(composition.getId());
            playQueueEntity.setPosition(i);

            entityList.add(playQueueEntity);
        }
        return entityList;
    }

    private List<PlayQueueEntityNew> toEntityList(List<Composition> compositions,
                                                  Random random) {
        List<PlayQueueEntityNew> entityList = new ArrayList<>(compositions.size());
        List<Integer> shuffledPositionList = new ArrayList<>(compositions.size());
        for (int i = 0; i < compositions.size(); i++) {
            shuffledPositionList.add(i);
        }
        Collections.shuffle(shuffledPositionList, random);

        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            PlayQueueEntityNew playQueueEntity = new PlayQueueEntityNew();
            playQueueEntity.setAudioId(composition.getId());
            playQueueEntity.setPosition(i);
            playQueueEntity.setShuffledPosition(shuffledPositionList.get(i));

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

    private List<PlayQueueItem> toPlayQueueItems(List<Composition> compositions, long[] ids) {
        List<PlayQueueItem> items = new ArrayList<>();

        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            PlayQueueItem playQueueItem = new PlayQueueItem(ids[i], composition);
            items.add(playQueueItem);
        }
        return items;
    }
}
