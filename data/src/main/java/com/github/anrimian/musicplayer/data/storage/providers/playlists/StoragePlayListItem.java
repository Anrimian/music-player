package com.github.anrimian.musicplayer.data.storage.providers.playlists;

public class StoragePlayListItem {

    private long itemId;
    private long compositionId;

    public StoragePlayListItem(long itemId, long compositionId) {
        this.itemId = itemId;
        this.compositionId = compositionId;
    }

    public long getItemId() {
        return itemId;
    }

    public long getCompositionId() {
        return compositionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoragePlayListItem that = (StoragePlayListItem) o;

        return itemId == that.itemId;
    }

    @Override
    public int hashCode() {
        return (int) (itemId ^ (itemId >>> 32));
    }
}
