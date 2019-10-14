package com.github.anrimian.musicplayer.domain.models.playlist;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayListItem {

    private final long itemId;

    @Nullable
    private final Long storageItemId;

    @Nonnull
    private final Composition composition;

    public PlayListItem(long itemId,
                        @Nullable Long storageItemId,
                        @Nonnull Composition composition) {
        this.itemId = itemId;
        this.storageItemId = storageItemId;
        this.composition = composition;
    }

    @Nullable
    public Long getStorageItemId() {
        return storageItemId;
    }

    public long getItemId() {
        return itemId;
    }

    @Nonnull
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
