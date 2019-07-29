package com.github.anrimian.musicplayer.domain.models.composition;

import javax.annotation.Nullable;

/**
 * Created on 01.05.2018.
 */
public class PlayQueueEvent {

    @Nullable
    private final PlayQueueItem playQueueItem;

    private final long trackPosition;
    private final boolean takePositionFromCurrent;

    public PlayQueueEvent(PlayQueueItem playQueueItem) {
        this(playQueueItem, 0);
    }

    public PlayQueueEvent(PlayQueueItem playQueueItem, boolean takePositionFromCurrent) {
        this(playQueueItem, 0, takePositionFromCurrent);
    }

    public PlayQueueEvent(@Nullable PlayQueueItem playQueueItem, long trackPosition) {
        this(playQueueItem, trackPosition, false);
    }

    public PlayQueueEvent(@Nullable PlayQueueItem playQueueItem,
                          long trackPosition,
                          boolean takePositionFromCurrent) {
        this.playQueueItem = playQueueItem;
        this.trackPosition = trackPosition;
        this.takePositionFromCurrent = takePositionFromCurrent;
    }

    @Nullable
    public PlayQueueItem getPlayQueueItem() {
        return playQueueItem;
    }

    public long getTrackPosition() {
        return trackPosition;
    }

    public boolean takePositionFromCurrent() {
        return takePositionFromCurrent;
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
