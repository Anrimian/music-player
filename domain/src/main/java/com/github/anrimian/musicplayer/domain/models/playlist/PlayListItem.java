package com.github.anrimian.musicplayer.domain.models.playlist;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class PlayListItem {

    private final long itemId;

    private final Composition composition;

    public PlayListItem(long itemId, Composition composition) {
        this.itemId = itemId;
        this.composition = composition;
    }

    public long getItemId() {
        return itemId;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayListItem that = (PlayListItem) o;

        return itemId == that.itemId;
    }

    @Override
    public int hashCode() {
        return (int) (itemId ^ (itemId >>> 32));
    }

    @Override
    public String toString() {
        return "PlayListItem{" +
                "itemId=" + itemId +
                ", composition=" + composition +
                '}';
    }
}
