package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class PlayQueue {

    private final List<Composition> compositionQueue;
    private final List<Composition> shuffledQueue;
    private final Map<Long, Integer> positionMap = new HashMap<>();
    private final Map<Long, Integer> shuffledPositionMap = new HashMap<>();

    private boolean shuffled;

    PlayQueue(List<Composition> compositions, boolean shuffled) {
        compositionQueue = compositions;
        shuffledQueue = new ArrayList<>(compositionQueue);
        this.shuffled = shuffled;
        Collections.shuffle(shuffledQueue);
        fillPositionMap();
    }

    PlayQueue(List<Composition> compositionQueue,
              List<Composition> shuffledQueue,
              boolean shuffled) {
        this.compositionQueue = compositionQueue;
        this.shuffledQueue = shuffledQueue;
        this.shuffled = shuffled;
        fillPositionMap();
    }

    @Nullable
    public Integer getPosition(@Nullable Composition composition) {
        if (composition == null) {
            return null;
        }
        Map<Long, Integer> positionMap = shuffled? shuffledPositionMap: this.positionMap;
        return positionMap.get(composition.getId());
    }

    public List<Composition> getCurrentPlayQueue() {
        return shuffled? shuffledQueue: compositionQueue;
    }

    public List<Composition> getCompositionQueue() {
        return compositionQueue;
    }

    public List<Composition> getShuffledQueue() {
        return shuffledQueue;
    }

    public void changeShuffleMode(boolean shuffled) {
        this.shuffled = shuffled;
        if (shuffled) {
            Collections.shuffle(shuffledQueue);
        }
        fillPositionMap();//TODO optimize
    }

    public void moveCompositionToTopInShuffledList(Composition composition) {
        if (!shuffled) {
            throw new IllegalStateException("move composition to top without shuffle mode");
        }

        int previousPosition = shuffledPositionMap.get(composition.getId());
        Composition firstComposition = shuffledQueue.set(0, composition);
        shuffledQueue.set(previousPosition, firstComposition);

        shuffledPositionMap.put(composition.getId(), 0);
        shuffledPositionMap.put(firstComposition.getId(), previousPosition);
    }

    @Nullable
    public Composition getCompositionById(Long id) {
        Integer position = positionMap.get(id);
        if (position == null) {
            return null;
        }
        return compositionQueue.get(position);
    }

    public boolean isEmpty() {
        return compositionQueue.isEmpty();
    }

    public void deleteCompositions(List<Composition> compositions) {
        Composition[] normalList = new Composition[compositionQueue.size()];
        compositionQueue.toArray(normalList);

        Composition[] shuffledList = new Composition[shuffledQueue.size()];
        shuffledQueue.toArray(shuffledList);

        for (Composition composition: compositions) {
            int position = positionMap.get(composition.getId());
            normalList[position] = null;

            int secondaryPosition = shuffledPositionMap.get(composition.getId());
            shuffledList[secondaryPosition] = null;
        }

        compositionQueue.clear();
        for (Composition composition: normalList) {
            if (composition != null) {
                compositionQueue.add(composition);
            }
        }

        shuffledQueue.clear();
        for (Composition composition: shuffledList) {
            if (composition != null) {
                shuffledQueue.add(composition);
            }
        }

        fillPositionMap();
    }

    public void updateComposition(Composition composition) {
        compositionQueue.set(positionMap.get(composition.getId()), composition);
        shuffledQueue.set(shuffledPositionMap.get(composition.getId()), composition);
    }

    private void fillPositionMap() {
        positionMap.clear();
        for (int i = 0; i < compositionQueue.size(); i++) {
            Composition composition = compositionQueue.get(i);
            long id = composition.getId();
            positionMap.put(id, i);
        }

        shuffledPositionMap.clear();
        for (int i = 0; i < shuffledQueue.size(); i++) {
            Composition composition = shuffledQueue.get(i);
            long id = composition.getId();
            shuffledPositionMap.put(id, i);
        }
    }
}
