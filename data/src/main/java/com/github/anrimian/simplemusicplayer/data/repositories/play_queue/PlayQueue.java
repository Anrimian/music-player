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

    private final Map<Long, Integer> initialPlayList;
    private final Map<Long, Integer> shuffledPlayList;
    private final Map<Long, Composition> compositionMap;

    PlayQueue(List<Composition> compositions) {
        compositionMap = new HashMap<>(compositions.size());
        initialPlayList = new HashMap<>(compositions.size());
        shuffledPlayList = new HashMap<>(compositions.size());
        for (int i = 0; i < compositions.size(); i++) {
            Composition composition = compositions.get(i);
            long id = composition.getId();
            compositionMap.put(id, composition);
            initialPlayList.put(id, i);
        }

        shuffle();
    }

    PlayQueue(Map<Long, Integer> initialPlayList,
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

    public void shuffle() {
        shuffledPlayList.clear();

        List<Integer> valueList = new ArrayList<>(initialPlayList.values());
        Collections.shuffle(valueList);

        Iterator<Integer> valueIterator = valueList.iterator();
        for (Long id: initialPlayList.keySet()) {
            shuffledPlayList.put(id, valueIterator.next());
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

        int freeShuffledPosition = shuffledPlayList.remove(id);
        for (Map.Entry<Long, Integer> entry:  shuffledPlayList.entrySet()) {
            if (entry.getValue() > freeShuffledPosition) {
                shuffledPlayList.put(entry.getKey(), entry.getValue() - 1);
            }
        }

        int freeInitialPosition = initialPlayList.remove(id);
        for (Map.Entry<Long, Integer> entry:  initialPlayList.entrySet()) {
            if (entry.getValue() > freeInitialPosition) {
                initialPlayList.put(entry.getKey(), entry.getValue() - 1);
            }
        }
    }

    public void moveCompositionToTopInShuffledList(Composition composition) {
        long id = composition.getId();
        for (Map.Entry<Long, Integer> entry: shuffledPlayList.entrySet()) {
            if (entry.getValue() == 0) {
                int previousPosition = shuffledPlayList.put(id, 0);
                shuffledPlayList.put(entry.getKey(), previousPosition);
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
