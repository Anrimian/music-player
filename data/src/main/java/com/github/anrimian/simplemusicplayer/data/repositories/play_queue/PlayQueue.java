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

    public int getPosition(Composition composition) {
        return positionMap.get(composition.getId());
    }

    public List<Composition> getCurrentPlayQueue() {
        return new ArrayList<>(shuffled? shuffledQueue: compositionQueue);
    }

    public List<Composition> getCompositionQueue() {
        return compositionQueue;
    }

    public List<Composition> getShuffledQueue() {
        return shuffledQueue;
    }

    public void changeShuffleMode(boolean shuffled) {
        this.shuffled = shuffled;

        positionMap.clear();
        fillPositionMap();
    }

    public void moveCompositionToTopInShuffledList(Composition composition) {
        if (!shuffled) {
            throw new IllegalStateException("move composition to top without shuffle mode");
        }

        int previousPosition = positionMap.get(composition.getId());
        Composition firstComposition = shuffledQueue.set(0, composition);
        shuffledQueue.set(previousPosition, firstComposition);

        positionMap.put(composition.getId(), 0);
        positionMap.put(firstComposition.getId(), previousPosition);
    }

    @Nullable
    public Composition getCompositionById(Long id) {
        Integer position = positionMap.get(id);
        if (position == null) {
            return null;
        }
        return getCurrentPlayQueue().get(position);
    }

    public boolean isEmpty() {
        return compositionQueue.isEmpty();
    }

    public void deleteComposition(long id) {
        List<Composition> currentList = shuffled? shuffledQueue: compositionQueue;
        List<Composition> secondaryList = shuffled? compositionQueue: shuffledQueue;

        int position = positionMap.get(id);
        Composition composition = currentList.remove(position);

        secondaryList.remove(composition);

        positionMap.clear();
        fillPositionMap();
    }

    public void updateComposition(Composition composition) {
        List<Composition> currentList = shuffled? shuffledQueue: compositionQueue;
        List<Composition> secondaryList = shuffled? compositionQueue: shuffledQueue;

        int position = positionMap.get(composition.getId());
        currentList.set(position, composition);

        secondaryList.set(secondaryList.indexOf(composition), composition);
    }

    private void fillPositionMap() {
        List<Composition> compositions = shuffled? shuffledQueue: compositionQueue;
        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            long id = composition.getId();
            positionMap.put(id, i);
        }
    }
}
