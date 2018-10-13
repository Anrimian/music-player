package com.github.anrimian.musicplayer.data.repositories.play_queue;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class PlayQueue {

    private final List<PlayQueueItem> compositionQueue;
    private final List<PlayQueueItem> shuffledQueue;
    private final Map<Long, Integer> itemPositionMap = new HashMap<>();
    private final Map<Long, Integer> shuffledItemPositionMap = new HashMap<>();
    private final Map<Long, List<Integer>> compositionPositionsMap = new HashMap<>();
    private final Map<Long, List<Integer>> compositionShuffledPositionsMap = new HashMap<>();

    private boolean shuffled;

    PlayQueue(List<PlayQueueItem> compositionQueue,
              List<PlayQueueItem> shuffledQueue,
              boolean shuffled) {
        this.compositionQueue = compositionQueue;
        this.shuffledQueue = shuffledQueue;
        this.shuffled = shuffled;
        fillPositionMap();
    }

    @Nullable
    Integer getPosition(@Nullable PlayQueueItem item) {
        if (item == null) {
            return null;
        }
        Map<Long, Integer> positionMap = shuffled? shuffledItemPositionMap : itemPositionMap;
        return positionMap.get(item.getId());
    }

    List<PlayQueueItem> getCurrentPlayQueue() {
        return shuffled? shuffledQueue: compositionQueue;
    }

    List<PlayQueueItem> getCompositionQueue() {
        return compositionQueue;
    }

    List<PlayQueueItem> getShuffledQueue() {
        return shuffledQueue;
    }

    void changeShuffleMode(boolean shuffled) {
        this.shuffled = shuffled;
        if (shuffled) {
            Collections.shuffle(shuffledQueue);
        }
        fillPositionMap();//TODO optimize
    }

    void moveItemToTopInShuffledList(PlayQueueItem item) {
        if (!shuffled) {
            throw new IllegalStateException("move item to top without shuffle mode");
        }

        int previousPosition = shuffledItemPositionMap.get(item.getId());
        PlayQueueItem firstItem = shuffledQueue.set(0, item);
        shuffledQueue.set(previousPosition, firstItem);

        shuffledItemPositionMap.put(item.getId(), 0);
        shuffledItemPositionMap.put(firstItem.getId(), previousPosition);

        replacePosition(previousPosition, 0, item.getComposition(), compositionShuffledPositionsMap);
        replacePosition(0, previousPosition, firstItem.getComposition(), compositionShuffledPositionsMap);
    }

    public boolean isEmpty() {
        return compositionQueue.isEmpty();
    }

    boolean deleteCompositions(List<Composition> compositions) {
        boolean updated = false;

        PlayQueueItem[] normalList = new PlayQueueItem[compositionQueue.size()];
        compositionQueue.toArray(normalList);

        PlayQueueItem[] shuffledList = new PlayQueueItem[shuffledQueue.size()];
        shuffledQueue.toArray(shuffledList);

        for (Composition composition: compositions) {
            List<Integer> positions = compositionPositionsMap.get(composition.getId());
            if (positions == null) {
                continue;
            }
            updated = true;
            for (int position: positions) {
                normalList[position] = null;
            }
            for (int position: compositionShuffledPositionsMap.get(composition.getId())) {
                shuffledList[position] = null;
            }
        }

        compositionQueue.clear();
        for (PlayQueueItem composition: normalList) {
            if (composition != null) {
                compositionQueue.add(composition);
            }
        }

        shuffledQueue.clear();
        for (PlayQueueItem composition: shuffledList) {
            if (composition != null) {
                shuffledQueue.add(composition);
            }
        }

        fillPositionMap();

        return updated;
    }

    boolean updateComposition(Composition composition) {
        List<Integer> positions = compositionPositionsMap.get(composition.getId());
        if (positions == null) {
            return false;
        }
        for (int position: positions) {
            PlayQueueItem playQueueItem = compositionQueue.get(position);
            playQueueItem.setComposition(composition);
        }
        for (int position: compositionShuffledPositionsMap.get(composition.getId())) {
            PlayQueueItem playQueueItem = shuffledQueue.get(position);
            playQueueItem.setComposition(composition);
        }
        return true;
    }

    private void replacePosition(int oldPosition,
                                 int newPosition,
                                 Composition composition,
                                 Map<Long, List<Integer>> positionMap) {
        List<Integer> positions = positionMap.get(composition.getId());
        for (int i = 0; i < positions.size(); i++) {
            int position = positions.get(i);
            if (position == oldPosition) {
                positions.set(i, newPosition);
            }
        }
    }

    private void fillPositionMap() {
        itemPositionMap.clear();
        compositionPositionsMap.clear();
        for (int i = 0; i < compositionQueue.size(); i++) {
            PlayQueueItem item = compositionQueue.get(i);
            long id = item.getId();
            itemPositionMap.put(id, i);

            addToPositionMap(item.getComposition(), i, compositionPositionsMap);
        }

        shuffledItemPositionMap.clear();
        compositionShuffledPositionsMap.clear();
        for (int i = 0; i < shuffledQueue.size(); i++) {
            PlayQueueItem item = shuffledQueue.get(i);
            long id = item.getId();
            shuffledItemPositionMap.put(id, i);

            addToPositionMap(item.getComposition(), i, compositionShuffledPositionsMap);
        }
    }

    private void addToPositionMap(Composition composition,
                                  int position,
                                  Map<Long, List<Integer>> positionMap) {
        long compositionId = composition.getId();
        List<Integer> positions = positionMap.get(compositionId);
        if (positions == null) {
            positions = new ArrayList<>();
            positionMap.put(compositionId, positions);
        }
        positions.add(position);
    }
}
