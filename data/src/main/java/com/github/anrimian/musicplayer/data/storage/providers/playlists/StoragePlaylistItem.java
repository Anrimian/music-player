package com.github.anrimian.musicplayer.data.storage.providers.playlists;

public class StoragePlaylistItem {

    private final long itemId;
    private final long audioId;

    public StoragePlaylistItem(long itemId, long audioId) {
        this.itemId = itemId;
        this.audioId = audioId;
    }

    public long getItemId() {
        return itemId;
    }

    public long getAudioId() {
        return audioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoragePlaylistItem that = (StoragePlaylistItem) o;

        return itemId == that.itemId;
    }

    @Override
    public int hashCode() {
        return (int) (itemId ^ (itemId >>> 32));
    }
}