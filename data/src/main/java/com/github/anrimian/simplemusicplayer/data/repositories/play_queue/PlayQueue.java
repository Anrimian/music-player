package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayQueue {

    private final List<Composition> initialPlayList;
    private final List<Composition> shuffledPlayList;

    public PlayQueue(List<Composition> compositions) {
        initialPlayList = compositions;
        shuffledPlayList = new ArrayList<>(initialPlayList);
        Collections.shuffle(shuffledPlayList);
    }

    public List<Composition> getShuffledPlayList() {
        return shuffledPlayList;
    }

    public List<Composition> getInitialPlayList() {
        return initialPlayList;
    }
}
