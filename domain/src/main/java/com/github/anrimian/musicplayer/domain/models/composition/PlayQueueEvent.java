package com.github.anrimian.musicplayer.domain.models.composition;

import javax.annotation.Nullable;

/**
 * Created on 01.05.2018.
 */
public class PlayQueueEvent {

    @Nullable
    private final PlayQueueItem playQueueItem;

    private final long trackPosition;
    private final int queuePosition;

    public PlayQueueEvent(PlayQueueItem playQueueItem) {
        this(playQueueItem, 0, 0);
    }

    public PlayQueueEvent(@Nullable PlayQueueItem playQueueItem,
                          long trackPosition,
                          int queuePosition) {
        this.playQueueItem = playQueueItem;
        this.trackPosition = trackPosition;
        this.queuePosition = queuePosition;
    }

    @Nullable
    public PlayQueueItem getPlayQueueItem() {
        return playQueueItem;
    }

    public long getTrackPosition() {
        return trackPosition;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    @Override
    public String toString() {
        return "PlayQueueEvent{" +
                "playQueueItem=" + playQueueItem +
                ", trackPosition=" + trackPosition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayQueueEvent that = (PlayQueueEvent) o;

        return playQueueItem != null ? playQueueItem.equals(that.playQueueItem) : that.playQueueItem == null;
    }

    @Override
    public int hashCode() {
        return playQueueItem != null ? playQueueItem.hashCode() : 0;
    }
}
