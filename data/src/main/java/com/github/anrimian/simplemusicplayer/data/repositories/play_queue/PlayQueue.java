package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import android.util.Log;

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
    private final Map<Long, Integer> secondaryPositionMap = new HashMap<>();

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
    public Integer getPosition(Composition composition) {
        Log.d("KEK", "getPosition: " + composition + ", size:" + compositionQueue.size());
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

    public void deleteCompositions(List<Composition> compositions) {
        Composition[] currentList = new Composition[compositionQueue.size()];
        (shuffled? shuffledQueue: compositionQueue).toArray(currentList);

        Composition[] secondaryList = new Composition[compositionQueue.size()];
        (shuffled? compositionQueue: shuffledQueue).toArray(secondaryList);

        for (Composition composition: compositions) {
            int position = positionMap.get(composition.getId());
            currentList[position] = null;

            int secondaryPosition = secondaryPositionMap.get(composition.getId());
            secondaryList[secondaryPosition] = null;
        }

        compositionQueue.clear();
        for (Composition composition: currentList) {
            if (composition != null) {
                compositionQueue.add(composition);
            }
        }

        shuffledQueue.clear();
        for (Composition composition: secondaryList) {
            if (composition != null) {
                shuffledQueue.add(composition);
            }
        }

        fillPositionMap();
    }

    public void updateComposition(Composition composition) {
        List<Composition> currentList = shuffled? shuffledQueue: compositionQueue;
        List<Composition> secondaryList = shuffled? compositionQueue: shuffledQueue;

        currentList.set(positionMap.get(composition.getId()), composition);
        secondaryList.set(secondaryPositionMap.get(composition.getId()), composition);
    }

    private void fillPositionMap() {
        positionMap.clear();
        secondaryPositionMap.clear();

        List<Composition> currentList = shuffled? shuffledQueue: compositionQueue;
        for (int i = 0; i < currentList.size(); i++) {
            Composition composition = currentList.get(i);
            long id = composition.getId();
            positionMap.put(id, i);
        }

        List<Composition> secondaryList = shuffled? compositionQueue: shuffledQueue;
        for (int i = 0; i < secondaryList.size(); i++) {
            Composition composition = secondaryList.get(i);
            long id = composition.getId();
            secondaryPositionMap.put(id, i);
        }
    }
}
