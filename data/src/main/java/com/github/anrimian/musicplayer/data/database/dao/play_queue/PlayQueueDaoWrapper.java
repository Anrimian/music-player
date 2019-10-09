package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueCompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

    public Flowable<List<PlayQueueCompositionEntity>> getPlayQueueObservable() {
        return playQueueDao.getPlayQueueObservable();
    }

    public PlayQueueCompositionEntity getPlayQueueItem(long id) {
        return playQueueDao.getPlayQueueEntity(id);
    }

    public void reshuffleQueue(PlayQueueItem currentItem) {
        appDatabase.runInTransaction(() -> {
            List<PlayQueueEntity> list = playQueueDao.getPlayQueue();

//            long[] ids = playQueueDao.getQueueIdsInRandomOrder();
            Collections.shuffle(list);

            long firstItemId = list.get(0).getId();
            long currentItemId = currentItem.getId();
            int currentItemPosition = -1;
            for (int i = 0; i < list.size(); i++) {
                PlayQueueEntity entity = list.get(i);

//                long id = ids[i];
                if (entity.getId() == currentItemId) {
                    currentItemPosition = i;
                }
                entity.setShuffledPosition(i);
//                playQueueDao.updateShuffledPosition(ids[i], i);
            }
            playQueueDao.update(list);

            if (currentItemPosition != -1 && firstItemId != currentItemId) {
                playQueueDao.updateShuffledPosition(currentItemId, 0);
                playQueueDao.updateShuffledPosition(firstItemId, currentItemPosition);
            }
        });
    }

    public PlayQueueLists insertNewPlayQueue(List<Composition> compositions) {
        return appDatabase.runInTransaction(() -> {
            List<Composition> shuffledList = new ArrayList<>(compositions);
            long randomSeed = System.nanoTime();
            Collections.shuffle(shuffledList, new Random(randomSeed));

            playQueueDao.deletePlayQueue();
            List<PlayQueueEntity> entities = toEntityList(compositions, randomSeed);
            List<Long> ids = playQueueDao.insertItems(entities);

            List<PlayQueueItem> items = toPlayQueueItems(compositions, ids);
            List<Long> shuffledIds = new ArrayList<>(ids);
            Collections.shuffle(shuffledIds, new Random(randomSeed));
            List<PlayQueueItem> shuffledItems = toPlayQueueItems(shuffledList, shuffledIds);

            return new PlayQueueLists(items, shuffledItems);
        });
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

    public void addCompositionsToEndQueue(List<Composition> compositions) {
        appDatabase.runInTransaction(() -> {
            int positionToInsert = playQueueDao.getLastPosition();
            List<PlayQueueEntity> entities = toEntityList(compositions, positionToInsert, positionToInsert);
            playQueueDao.insertItems(entities);
        });
    }

    public void addCompositionsToQueue(List<Composition> compositions,
                                                      PlayQueueItem currentItem) {
        appDatabase.runInTransaction(() -> {
            int position = playQueueDao.getPosition(currentItem.getId());
            int shuffledPosition = playQueueDao.getShuffledPosition(currentItem.getId());

            int increaseBy = compositions.size();
            playQueueDao.increasePositions(increaseBy, position);
            playQueueDao.increaseShuffledPositions(increaseBy, shuffledPosition);

            List<PlayQueueEntity> entities = toEntityList(compositions, position, shuffledPosition);
            playQueueDao.insertItems(entities);
        });
    }

    private List<PlayQueueEntity> toEntityList(List<Composition> compositions,
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
