package com.github.anrimian.musicplayer.data.database.entities.play_queue;

import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import java.util.List;

public class PlayQueueLists {

    private final List<PlayQueueItem> queue;
    private final List<PlayQueueItem> shuffledQueue;

    public PlayQueueLists(List<PlayQueueItem> queue, List<PlayQueueItem> shuffledQueue) {
        this.queue = queue;
        this.shuffledQueue = shuffledQueue;
    }

    public List<PlayQueueItem> getQueue() {
        return queue;
    }

    public List<PlayQueueItem> getShuffledQueue() {
        return shuffledQueue;
    }
}
