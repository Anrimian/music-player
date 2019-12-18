package com.github.anrimian.musicplayer.data.repositories.play_queue;

import com.github.anrimian.musicplayer.data.utils.collections.IndexedList;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.utils.java.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


class PlayQueueCache {

    private final Function<IndexedList<PlayQueueItem>> listFetcher;

    @Nullable
    private IndexedList<PlayQueueItem> currentQueue;

    PlayQueueCache(Function<IndexedList<PlayQueueItem>> listFetcher) {
        this.listFetcher = listFetcher;
    }

    @Nonnull
    IndexedList<PlayQueueItem> getCurrentQueue() {
        if (currentQueue == null) {
            synchronized (this) {
                if (currentQueue == null) {
                    currentQueue = listFetcher.call();
                }
            }
        }
        return currentQueue;
    }

    void updateQueue(IndexedList<PlayQueueItem> currentQueue) {
        this.currentQueue = currentQueue;
    }

}
