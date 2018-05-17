package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayQueue {

    private final Map<Long, Integer> initialPlayList;
    private final Map<Long, Integer> shuffledPlayList;
    private final Map<Long, Composition> compositionMap;

    public PlayQueue(List<Composition> compositions) {
        compositionMap = new HashMap<>(compositions.size());
        initialPlayList = new HashMap<>(compositions.size());
        shuffledPlayList = new HashMap<>(compositions.size());
        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            long id = composition.getId();
            compositionMap.put(id, composition);
            initialPlayList.put(id, i);
            shuffledPlayList.put(id, i);
        }

        shuffle();
    }

    public PlayQueue(Map<Long, Integer> initialPlayList,
                     Map<Long, Integer> shuffledPlayList,
                     Map<Long, Composition> compositionMap) {
        this.initialPlayList = initialPlayList;
        this.shuffledPlayList = shuffledPlayList;
        this.compositionMap = compositionMap;
    }

    public Map<Long, Composition> getCompositionMap() {
        return compositionMap;
    }

    public int getPosition(Composition composition) {
        return initialPlayList.get(composition.getId());
    }

    public int getShuffledPosition(Composition composition) {
        return shuffledPlayList.get(composition.getId());
    }

    public List<Composition> getShuffledPlayList() {
        return createList(shuffledPlayList);
    }

    public List<Composition> getInitialPlayList() {
        return createList(initialPlayList);
    }

    public Map<Long, Integer> getShuffledPlayMap() {
        return shuffledPlayList;
    }

    public Map<Long, Integer> getInitialPlayMap() {
        return initialPlayList;
    }

    public void shuffle() {
        List<Integer> valueList = new ArrayList<>(initialPlayList.values());
        Collections.shuffle(valueList);

        Iterator<Integer> valueIterator = valueList.iterator();
        for (Long id: initialPlayList.keySet()) {
            shuffledPlayList.put(id, valueIterator.next());
        }
    }

    private List<Composition> createList(Map<Long, Integer> positionMap) {
        Composition[] compositions = new Composition[positionMap.size()];
        for (long id: positionMap.values()) {
            compositions[positionMap.get(id)] = compositionMap.get(id);
        }
        return Arrays.asList(compositions);
    }
}
