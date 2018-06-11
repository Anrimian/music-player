package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class PlayQueue {

    private final Map<Long, Integer> positionMap;
    private final Map<Long, Integer> shuffledPositionMap;
    private final Map<Long, Composition> compositionMap;

    PlayQueue(List<Composition> compositions) {
        compositionMap = new HashMap<>(compositions.size());
        positionMap = new HashMap<>(compositions.size());
        shuffledPositionMap = new HashMap<>(compositions.size());
        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            long id = composition.getId();
            compositionMap.put(id, composition);
            positionMap.put(id, i);
        }
        shuffle();
    }

    PlayQueue(Map<Long, Integer> positionMap,
              Map<Long, Integer> shuffledPositionMap,
              Map<Long, Composition> compositionMap) {
        this.positionMap = positionMap;
        this.shuffledPositionMap = shuffledPositionMap;
        this.compositionMap = compositionMap;
    }

    public Map<Long, Composition> getCompositionMap() {
        return compositionMap;
    }

    public int getPosition(Composition composition) {
        return positionMap.get(composition.getId());
    }

    public int getShuffledPosition(Composition composition) {
        return shuffledPositionMap.get(composition.getId());
    }

    public List<Composition> getShuffledPlayList() {
        return createList(shuffledPositionMap);
    }

    public List<Composition> getPlayList() {
        return createList(positionMap);
    }

    public Map<Long, Integer> getPositionMap() {
        return positionMap;
    }

    public Map<Long, Integer> getShuffledPositionMap() {
        return shuffledPositionMap;
    }

    public void shuffle() {
        shuffledPositionMap.clear();

        List<Integer> valueList = new ArrayList<>(positionMap.values());
        Collections.shuffle(valueList);

        Iterator<Integer> valueIterator = valueList.iterator();
        for (Long id: positionMap.keySet()) {
            shuffledPositionMap.put(id, valueIterator.next());
        }
    }

    @Nullable
    public Composition getCompositionById(Long id) {
        return compositionMap.get(id);
    }

    public boolean isEmpty() {
        return compositionMap.isEmpty();
    }

    public void deleteComposition(long id) {
        compositionMap.remove(id);
    }

    public void moveCompositionToTopInShuffledList(Composition composition) {
        long id = composition.getId();
        for (Map.Entry<Long, Integer> entry: shuffledPositionMap.entrySet()) {
            if (entry.getValue() == 0) {
                int previousPosition = shuffledPositionMap.put(id, 0);
                shuffledPositionMap.put(entry.getKey(), previousPosition);
                break;
            }
        }
    }

    public void updateComposition(Composition modifiedComposition) {
        compositionMap.put(modifiedComposition.getId(), modifiedComposition);
    }

    private List<Composition> createList(Map<Long, Integer> positionMap) {
        Composition[] compositions = new Composition[positionMap.size()];
        for (long id: positionMap.keySet()) {
            compositions[positionMap.get(id)] = compositionMap.get(id);
        }
        return new ArrayList<>(Arrays.asList(compositions));
    }


}
