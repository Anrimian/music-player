package com.github.anrimian.musicplayer.data.models.composition;

import javax.annotation.Nullable;

public class CompositionId {

    private final long id;

    @Nullable
    private final Long storageId;

    @SuppressWarnings("NullableProblems")
    public CompositionId(long id, Long storageId) {
        this.id = id;
        this.storageId = storageId;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositionId that = (CompositionId) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
