package com.github.anrimian.musicplayer.data.database.entities;

import javax.annotation.Nullable;

public class IdPair {

    private final long dbId;

    @Nullable
    private final Long storageId;

    public IdPair(long dbId, @Nullable Long storageId) {
        this.dbId = dbId;
        this.storageId = storageId;
    }

    public long getDbId() {
        return dbId;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }
}
