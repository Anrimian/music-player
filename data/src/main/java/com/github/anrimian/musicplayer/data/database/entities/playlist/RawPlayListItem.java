package com.github.anrimian.musicplayer.data.database.entities.playlist;

import javax.annotation.Nullable;

public class RawPlayListItem {

    @Nullable
    private Long storageItemId;

    private long audioId;

    public RawPlayListItem(@Nullable Long storageItemId, long audioId) {
        this.storageItemId = storageItemId;
        this.audioId = audioId;
    }

    @Nullable
    public Long getStorageItemId() {
        return storageItemId;
    }

    public long getAudioId() {
        return audioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RawPlayListItem that = (RawPlayListItem) o;

        if (audioId != that.audioId) return false;
        return storageItemId != null ? storageItemId.equals(that.storageItemId) : that.storageItemId == null;
    }

    @Override
    public int hashCode() {
        int result = storageItemId != null ? storageItemId.hashCode() : 0;
        result = 31 * result + (int) (audioId ^ (audioId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "RawPlayListItem{" +
                "storageItemId=" + storageItemId +
                ", audioId=" + audioId +
                '}';
    }
}
