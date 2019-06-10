package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
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

    public PlayQueueLists insertNewPlayQueue(List<Composition> compositions) {
        return appDatabase.runInTransaction(() -> {
            List<Composition> shuffledList = new ArrayList<>(compositions);
            long randomSeed = System.nanoTime();
            Collections.shuffle(shuffledList, new Random(randomSeed));
            return insertNewPlayQueue(compositions, shuffledList, randomSeed);
        });
    }

    public PlayQueueLists getPlayQueue(Function<Map<Long, Composition>> compositionsCallable) {
        return toPlayQueueLists(playQueueDao.getPlayQueue(), compositionsCallable.call());
    }

    public void deleteItem(long itemId) {
        playQueueDao.deleteItem(itemId);
    }

    public void swapItems(PlayQueueItem firstItem,
                          int firstPosition,
                          PlayQueueItem secondItem,
                          int secondPosition,
                          boolean shuffleMode) {
        appDatabase.runInTransaction(() -> {
            if (shuffleMode) {
                playQueueDao.updateShuffledPosition(firstItem.getId(), secondPosition);
                playQueueDao.updateShuffledPosition(secondItem.getId(), firstPosition);
            } else {
                playQueueDao.updateItemPosition(firstItem.getId(), secondPosition);
                playQueueDao.updateItemPosition(secondItem.getId(), firstPosition);
            }
        });
    }

    public List<PlayQueueItem> addCompositionsToEndQueue(List<Composition> compositions) {
        return appDatabase.runInTransaction(() -> {
            int positionToInsert = playQueueDao.getLastPosition();
            List<PlayQueueEntity> entities = toEntityList(compositions, positionToInsert, positionToInsert);
            List<Long> ids = playQueueDao.insertItems(entities);
            return toPlayQueueItems(compositions, ids);
        });
    }

    public List<PlayQueueItem> addCompositionsToQueue(List<Composition> compositions,
                                                      PlayQueueItem currentItem) {
        return appDatabase.runInTransaction(() -> {
            int position = playQueueDao.getPosition(currentItem.getId());
            int shuffledPosition = playQueueDao.getShuffledPosition(currentItem.getId());

            int increaseBy = compositions.size();
            playQueueDao.increasePositions(increaseBy, position);
            playQueueDao.increaseShuffledPositions(increaseBy, shuffledPosition);

            List<PlayQueueEntity> entities = toEntityList(compositions, position, shuffledPosition);
            List<Long> ids = playQueueDao.insertItems(entities);
            return toPlayQueueItems(compositions, ids);
        });
    }

    PlayQueueLists insertNewPlayQueue(List<Composition> compositions,
                                      List<Composition> shuffledCompositions,
                                      long randomSeed) {
        playQueueDao.deletePlayQueue();
        List<Long> ids = playQueueDao.insertItems(toShuffledEntityList(compositions, randomSeed));
        List<PlayQueueItem> items = toPlayQueueItems(compositions, ids);
        List<Long> shuffledIds = new ArrayList<>(ids);
        Collections.shuffle(shuffledIds, new Random(randomSeed));
        List<PlayQueueItem> shuffledItems = toPlayQueueItems(shuffledCompositions, shuffledIds);
        return new PlayQueueLists(items, shuffledItems);
    }

    private PlayQueueLists toPlayQueueLists(List<PlayQueueEntity> entities,
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

    private List<PlayQueueItem> toItems(List<PlayQueueEntity> entities,
                                        Map<Long, Composition> compositionMap) {
        List<PlayQueueItem> items = new ArrayList<>();
        for (PlayQueueEntity entity: entities) {
            Composition composition = compositionMap.get(entity.getAudioId());
            if (composition != null) {
                items.add(new PlayQueueItem(entity.getId(), composition));
            }
        }
        return items;
    }

    private void filterDeletedCompositions(List<PlayQueueEntity> entities,
                                           Map<Long, Composition> compositionMap) {
        Iterator<PlayQueueEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            PlayQueueEntity playQueueEntity = iterator.next();
            long audioId = playQueueEntity.getAudioId();
            Composition composition = compositionMap.get(audioId);
            if (composition == null) {
                playQueueDao.deleteItem(audioId);
                iterator.remove();
            }
        }
    }

    private List<PlayQueueEntity> toShuffledEntityList(List<Composition> compositions,
                                                       long randomSeed) {
        List<PlayQueueEntity> entityList = new ArrayList<>(compositions.size());
        List<Integer> shuffledPositionList = new ArrayList<>(compositions.size());
        for (int i = 0; i < compositions.size(); i++) {
            shuffledPositionList.add(i);
        }
        Collections.shuffle(shuffledPositionList, new Random(randomSeed));

        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();
            playQueueEntity.setAudioId(composition.getId());
            playQueueEntity.setPosition(i);
            playQueueEntity.setShuffledPosition(shuffledPositionList.get(i));

            entityList.add(playQueueEntity);
        }
        return entityList;
    }

    private List<PlayQueueEntity> toEntityList(List<Composition> compositions,
                                               int position,
                                               int shuffledPosition) {
        List<PlayQueueEntity> entityList = new ArrayList<>(compositions.size());

        for (Composition composition: compositions) {
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();
            playQueueEntity.setAudioId(composition.getId());
            playQueueEntity.setPosition(++position);
            playQueueEntity.setShuffledPosition(++shuffledPosition);

            entityList.add(playQueueEntity);

//            position++;
//            shuffledPosition++;
        }
        return entityList;
    }

    private List<PlayQueueItem> toPlayQueueItems(List<Composition> compositions, List<Long> ids) {
        List<PlayQueueItem> items = new ArrayList<>();

        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            PlayQueueItem playQueueItem = new PlayQueueItem(ids.get(i), composition);
            items.add(playQueueItem);
        }
        return items;
    }
}
